/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import java.util.List;
import connectors.database.IDbConnector;
import connectors.RouteEntry;
import com.owlike.genson.Genson;
import connectors.DataEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.toIntExact;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Simon
 */
public class WazeProviderConnector extends AProviderConnector {

    private static final Logger log = Logger.getLogger(WazeProviderConnector.class);
    protected Cookie csrf;
    protected Cookie web_session;
    private int bid = 0;
    protected boolean loggedIn = false;

    /**
     * List of all Future instances from last triggerUpdate (check
     * java.util.concurrent library). ± sort of threads Use f.get(); to wait the
     * thread to finish and return its value Future<ReturnType>
     * Return value = Boolean => succeeded = true
     */
    protected List<Future<Boolean>> buzyRequests;

    public WazeProviderConnector(IDbConnector dbConnector) {
        super(dbConnector, "Waze");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        updateInterval = Integer.parseInt(prop.getProperty("WAZE_UPDATE_INTERVAL"));
    }

    /**
     * @return boolean, true when csrf and web_session are set
     */
    private boolean areCookiesSet() {
        return csrf != null && web_session != null;
    }

    private void resetCookies() {
        csrf = null;
        web_session = null;
    }

    /**
     * Sets the required HTTP headers for a request to the Here API: - Disable
     * cache - Accept gzip - Set origin to waze.com - Accept language: Dutch,
     * English - Set token cookies, if any (csrf and web_session when
     * areCookiesSet) - Accept all media types
     *
     * @param request to set headers of
     */
    private void setHeaders(HttpRequest request) {
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        /*if (this.areCookiesSet()) {
            request.setHeader("X-CSRF-Token", csrf.getValue());
            request.addCookie(csrf);
            request.addCookie(web_session);
        }*/
        request.setHeader("Accept", "*/*");
    }

    /**
     * Maps wazeRouteId on RouteEntries from the DbConnector
     *
     * @param wazeRouteId searched for
     * @return RouteEntry connected to this wazeRouteId
     */
    private RouteEntry getRouteFromWazeRouteId(int wazeRouteId) {
        if (wazeRouteId < 4069 || wazeRouteId > 4098) {
            return null;
        }
        return dbConnector.findRouteEntryByID(wazeRouteId - 4068);
    }

    /**
     * Searches response for HTTP headers that set any cookies. Check if they
     * set _csrf_token and _web_session. If so, save them in csrf and
     * web_session
     *
     * @param response
     */
    private void saveTokens(CloseableHttpClient client, CloseableHttpResponse response) {
        HttpClientContext context = HttpClientContext.create();
        CookieStore store = context.getCookieStore();
        List<Cookie> cookies = store.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("_csrf_token")) {
                csrf = cookie;
            } else if (cookie.getName().equals("_web_session")) {
                web_session = cookie;
            }
        }
    }

    /**
     * Calls the WAZE api synchronously. Asks WAZE if the current tokens
     * (cookies) are authorized. If no current cookies are set, the request will
     * create new tokens needed for logging in. Passes response object to
     * this.saveTokens to save these. Passes response body to
     * this.parseGetLoginJSON
     *
     * @return succeeded: got correct json response
     */
    private boolean getLogin(CloseableHttpClient client) {
        /*HttpGet request = new HttpGet(prop.getProperty("WAZE_API_URL_GET_LOGIN"));
        setHeaders(request);
        
        try(CloseableHttpResponse response = client.execute(request)){
            saveTokens(client, response);
            
        }catch(IOException e){
            log.error("getLogin() IOException: "+e.getMessage(), e);
        }
        
        BoundRequestBuilder request = a.prepareGet(prop.getProperty("WAZE_API_URL_GET_LOGIN"));
        this.setHeaders(request);

        Future<Boolean> f = request.execute(
                new AsyncCompletionHandler<Boolean>() {
            @Override
            public Boolean onCompleted(Response response) throws Exception {
                saveTokens(response);
                return parseGetLoginJSON(response.getResponseBody());
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            log.error("Thread getLogin interrupted", ex);
        } catch (ExecutionException ex) {
            log.error(ex, ex);
        }*/
        return false;
    }

    /**
     * Calls the WAZE api synchronously. Request all traffic data in JSON and
     * returns them parsed into DataEntries. Doesn't need tokens (weird). If the
     * response returns a 403, 401 status code, loggedIn will be set to false.
     * If not okay (200): throws RouteUnavailableException.
     *
     * @return List<DataEntry> parsed from JSON response from WAZE api.
     */
    private List<DataEntry> getData(CloseableHttpClient client) throws RouteUnavailableException, NotAuthorizedException {
        HttpGet request = new HttpGet(prop.getProperty("WAZE_API_URL_GET_DATA") + bid);
        this.setHeaders(request);

        List<DataEntry> ret = new ArrayList<>();
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 403 || response.getStatusLine().getStatusCode() == 401) {
                loggedIn = false;
                throw new NotAuthorizedException("Authentication failed: " + response.getStatusLine());
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RouteUnavailableException(providerName, "Failed getting data from Waze: " + response.getStatusLine());
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    json.append(line);
                }
                ret = fetchDataFromJSON(json.toString());
                rd.close();
                EntityUtils.consume(entity);
            }else{
                log.error("Entity was null, so getData returned an empty list.");
            }
        } catch (IOException e) {
            request.abort();
            log.error(e);
        } catch (NotAuthorizedException e) {
            request.abort();
            log.error(e);
        } catch (RouteUnavailableException e) {
            request.abort();
            log.error(e);
        }
        return ret;
    }

    /**
     * Calls the WAZE api synchronously. Request broadcasters of logged in user
     * (id needed to request trafficdata of a certain place) loggedIn set to
     * false when status 403, 401 Sends response to parseBidFromJSON which saves
     * the bid
     *
     * @return boolean: true when bid found
     */
    private boolean getBroadcasters() throws NotAuthorizedException {
        /*AsyncHttpClient a = new AsyncHttpClient();
        BoundRequestBuilder request = a.prepareGet(prop.getProperty("WAZE_API_URL_BROADCASTERS"));
        this.setHeaders(request);

        Future<Boolean> f = request.execute(
                new AsyncCompletionHandler<Boolean>() {
            @Override
            public Boolean onCompleted(Response response) throws Exception {
                if (response.getStatusCode() == 403 || response.getStatusCode() == 401) {
                    loggedIn = false;
                    log.info("getBroadcasters: " + response.getStatusText());
                    throw new NotAuthorizedException();
                }
                return parseBidFromJSON(response.getResponseBody());
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            log.error("Thread getBroadcasters intrerrupted");
            return false;
        } catch (ExecutionException ex) {
            log.error("ExecutionException");
            return false;
        }*/
        return false;
    }

    /**
     * Calls the WAZE api synchronously. Sign in to the Waze api and saves the
     * generated tokens for the next requests. Sets loggedIn = true when logged
     * in. Sets loggedIn = false if 401 / 403 received
     *
     * @return boolean: true if succeeded
     */
    private boolean createLogin() throws NotAuthorizedException {
        /*AsyncHttpClient a = new AsyncHttpClient();

        BoundRequestBuilder request = a.preparePost(prop.getProperty("WAZE_API_URL_LOGIN"));
        this.setHeaders(request);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.setBody("user_id=" + prop.getProperty("WAZE_USERNAME") + "&password=" + prop.getProperty("WAZE_PASSWORD"));

        Request q = request.build();

        Future<Boolean> f = request.execute(
                new AsyncCompletionHandler<Boolean>() {

            @Override
            public Boolean onCompleted(Response response) throws Exception {
                if (response.getStatusCode() == 200) {
                    saveTokens(response);
                    loggedIn = true;
                    return true;
                } else if (response.getStatusCode() == 403 || response.getStatusCode() == 401) {
                    loggedIn = false;
                    log.info("createLogin: " + response.getStatusText());
                    throw new NotAuthorizedException();
                }
                return false;
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            log.error("Thread createLogin interrupted");
            return false;
        } catch (ExecutionException ex) {
            log.error(ex, ex);
            return false;
        }*/
        return false;
    }

    /**
     * Calls the WAZE api synchronously. Destroy current tokens = sign out
     * Removes token cookies
     *
     * @return succeeded
     */
    private boolean destroyLogin() {
        /*BoundRequestBuilder request = a.preparePost(prop.getProperty("WAZE_API_URL_LOGOUT"));
        this.setHeaders(request);

        Future<Boolean> f = request.execute(
                new AsyncCompletionHandler<Boolean>() {
            @Override
            public Boolean onCompleted(Response response) throws Exception {
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    return true;
                }
                log.info("destroyLogin: " + response.getStatusText());
                return false;
            }
        });
        try {
            if (f.get()) {
                this.resetCookies();
                return true;
            }
        } catch (InterruptedException ex) {
            log.error("destroyLogin thread interrupted");
        } catch (ExecutionException ex) {
            log.error(ex, ex);
        }*/
        return false;
    }

    @Override
    public void triggerUpdate() {
        if (updateCounter % updateInterval == 0) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                callApi(client);
            } catch (RouteUnavailableException e) {

            } catch (IOException e) {

            }
        }
    }

    /**
     * Login if needed
     *
     * @return succeeded
     */
    private boolean loginIfNeeded(CloseableHttpClient client) {
        /*if (!areCookiesSet()) {
            if (!getLogin(client)) {
                return false;
            }
        }
        if (!loggedIn) {
            try {
                if (!createLogin()) {
                    return false;
                }
            } catch (NotAuthorizedException ex) {
                this.destroyLogin();
                return false;
            }
        }*/
        return true;
    }

    /**
     * Executed in thread. Gets data and saves it to the database (same as
     * triggerUpdate)
     *
     * @return succeeded
     */
    private boolean callApi(CloseableHttpClient client) throws RouteUnavailableException {
        // Structuur zodanig om aantal requests te beperken
        /*if (bid == 0) {
            loginIfNeeded(client);
            try {
                if (!getBroadcasters()) {
                    return false;
                }
            } catch (NotAuthorizedException ex) {
                // Try to retry the next time
                this.destroyLogin();
                return false;
            }
        }
         */
        // Data ophalen
        List<DataEntry> entries = new ArrayList<>();
        try {
            entries = getData(client);
        } catch (NotAuthorizedException ex) {
            // Opnieuw proberen inloggen en nog eens proberen
            /*destroyLogin();
            loginIfNeeded();
            try {
                entries = getData();
            } catch (NotAuthorizedException ex1) {
                // Ook 2e poging is mislukt
                this.destroyLogin(a);
                return false;
            }*/
        }
        for (DataEntry entry : entries) {
            this.dbConnector.insert(entry);
        }
        return true;
    }

    public List<DataEntry> fetchDataFromJSON(String json) throws RouteUnavailableException {
        List<DataEntry> entries = new ArrayList<>();
        try {
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);

            List<Object> routes = (List<Object>) map.get("routes");

            Map<Integer, RouteEntry> myRoutes = new TreeMap<>();
            for (Object r : routes) {
                Map<String, Object> route = (Map<String, Object>) r;
                // Hier haal ik nog extra optionele data op die we niet meteen gebruiken
                // Kan handig zijn voor later
                int time = toIntExact((long) route.get("time"));
                int htime = toIntExact((long) route.get("historicTime"));
                int id = toIntExact((long) route.get("id"));
                int jamlevel = toIntExact((long) route.get("jamLevel"));
                int length = toIntExact((long) route.get("length"));
                String name = (String) route.get("name");
                String toName = (String) route.get("toName");
                String fromName = (String) route.get("fromName");

                List<Object> line = (List<Object>) route.get("line");
                Map<String, Object> s = (Map<String, Object>) line.get(0);
                double sx = (double) s.get("x");
                double sy = (double) s.get("y");
                Map<String, Object> e = (Map<String, Object>) line.get(line.size() - 1);
                double ex = (double) e.get("x");
                double ey = (double) e.get("y");

                RouteEntry routeEntry = getRouteFromWazeRouteId(id);
                if (routeEntry != null) {
                    DataEntry entry = new DataEntry(time, routeEntry, providerEntry);
                    entries.add(entry);
                }
            }
        } catch (Exception ex) {
            log.error("Data unreadable. Expected other structure." + json, ex);
        }
        return entries;
    }

    public boolean parseGetLoginJSON(String json) {
        try {
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            Map<String, Object> reply = (Map<String, Object>) map.get("reply");
            long userid = (long) reply.get("user_id");
            this.loggedIn = (boolean) reply.get("login");
            String message = (String) reply.get("message");
            long error = (long) reply.get("user_id");
            long rank = (long) reply.get("user_id");
            String full_name = (String) reply.get("full_name");
            return true;
        } catch (Exception ex2) {
            log.error("Data from login JSON not readable: " + json, ex2);
        }
        return false;
    }

    public boolean parseBidFromJSON(String json) {
        try {
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            List<Object> broadcasters = (List<Object>) genson.deserialize(json, List.class);
            Object b = broadcasters.get(0);
            Map<String, Object> broadcaster = (Map<String, Object>) b;
            String name = (String) broadcaster.get("name"); // Verkeerscentrum Gent
            String env = (String) broadcaster.get("env"); // world
            int id = toIntExact((long) broadcaster.get("id"));
            this.bid = id;
            return true;
        } catch (Exception ex2) {
            log.error("Couldn't read BID from JSON: " + json, ex2);
            //this.doLog(Level.WARNING, "Couldn't read BID from JSON");
        }
        return false;
    }
}
