/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import java.util.List;
import connectors.database.IDbConnector;
import connectors.RouteEntry;
import com.ning.http.client.*;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.cookie.Cookie;
import com.owlike.genson.Genson;
import connectors.DataEntry;
import java.io.IOException;
import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import verkeer.Verkeer;

/**
 *
 * @author Simon
 */
public class WazeProviderConnector extends AProviderConnector {
    protected final static String API_URL = "https://route.cit.api.here.com/routing/7.2/calculateroute.json";
    protected final static String USERNAME = "VerkeerGent"; // Simon-Key
    protected final static String PASSWORD = "Paswoord1"; // Simon-Key
    protected Cookie csrf;
    protected Cookie web_session;
    private int bid = 0;
    protected boolean loggedIn = false;
   
    /**
     * List of all Future instances from last triggerUpdate (check java.util.concurrent library). 
     * ± sort of threads
     * Use f.get(); to wait the thread to finish and return its value Future<ReturnType>
     * Return value = Boolean => succeeded = true
     */
    protected List<Future<Boolean>> buzyRequests;
    
            
    public WazeProviderConnector(IDbConnector dbConnector) {
        super(dbConnector);
        buzyRequests = new ArrayList<>();
        String providerName = "Waze";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
    }
    
    /**
     * @return boolean, true when csrf and web_session are set
     */
    private boolean areCookiesSet(){
        return csrf != null && web_session != null;
    }
    
    private void resetCookies(){
        csrf = null;
        web_session = null;
    }
    
    /**
     * Sets the required HTTP headers for a request to the Here API: 
     * - Disable cache
     * - Accept gzip
     * - Set origin to waze.com
     * - Accept language: Dutch, English
     * - Set token cookies, if any (csrf and web_session when areCookiesSet)
     * - Accept all media types
     * 
     * @param request to set headers of
     */
    private void setHeaders(BoundRequestBuilder request){
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("X-Requested-With", "Java");
        request.addHeader("Referer", "https://www.waze.com/en/signin?redirect=/trafficview");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.addHeader("Accept-Language", "nl-NL,nl;q=0.8,en-US;q=0.6,en;q=0.4");
        request.addHeader("Origin", "https://www.waze.com");

        if (this.areCookiesSet()){
            request.setHeader("X-CSRF-Token", csrf.getValue());
            request.addCookie(csrf);
            request.addCookie(web_session);
        }
        request.setHeader("Accept", "*/*");
    }
    
    /**
     * Maps wazeRouteId on RouteEntries from the DbConnector
     * 
     * @param wazeRouteId searched for
     * @return RouteEntry connected to this wazeRouteId
     */
    private RouteEntry getRouteFromWazeRouteId(int wazeRouteId){
        // TODO: ID mapping implementeren op één of andere magische manier
        if (wazeRouteId < 4069 || wazeRouteId > 4098){
            return null;
        }
        return dbConnector.findRouteEntryByID(wazeRouteId-4068);
    }
    
    /**
     * Searches response for HTTP headers that set any cookies.
     * Check if they set _csrf_token and _web_session.
     * If so, save them in csrf and web_session
     * 
     * @param response
     */
    private void saveTokens(Response response){
        List<Cookie> cookies = response.getCookies();
        for (Cookie cookie : cookies){
            if (cookie.getName().equals("_csrf_token")){
                csrf = cookie;
            }
            else if (cookie.getName().equals("_web_session")){
                web_session = cookie;
            }
        }
    }
    
    /**
     * Calls the WAZE api synchronously.
     * Asks WAZE if the current tokens (cookies) are authorized.
     * If no current cookies are set, the request will create new tokens needed for logging in.
     * Passes response object to this.saveTokens to save these.
     * Passes response body to this.parseGetLoginJSON
     * 
     * @return succeeded: got correct json response
     */
    private boolean getLogin(){
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/login/get");
        this.setHeaders(request);

        Future<Boolean> f = request.execute(
           new AsyncCompletionHandler<Boolean>(){
            @Override
            public Boolean onCompleted(Response response) throws Exception{
                saveTokens(response);
                return parseGetLoginJSON(response.getResponseBody());
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            // TODO: loggen
        } catch (ExecutionException ex) {
            // TODO: loggen
        }
        return false;
    }
    
    /**
     * Calls the WAZE api synchronously.
     * Request all traffic data in JSON and returns them parsed into DataEntries.
     * Doesn't need tokens (weird).
     * If the response returns a 403, 401 status code, loggedIn will be set to false. 
     * If not okay (200): throws RouteUnavailableException.
     * 
     * @return List<DataEntry> parsed from JSON response from WAZE api.
     */   
    private List<DataEntry> getData() throws RouteUnavailableException, NotAuthorizedException{
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/row-rtserver/broadcast/BroadcastRSS?bid="+bid+"&format=JSON");
        this.setHeaders(request);
        
        Future<List<DataEntry>> f = request.execute(
           new AsyncCompletionHandler<List<DataEntry>>(){
            @Override
            public List<DataEntry> onCompleted(Response response) throws Exception{
                if (response.getStatusCode() == 403 || response.getStatusCode() == 401){
                    loggedIn = false;
                    throw new NotAuthorizedException("Authentication failed: "+response.getStatusText());
                }
                if (response.getStatusCode() != 200){
                    throw new RouteUnavailableException("Failed getting data from Waze: "+response.getStatusText());
                }
                return fetchDataFromJSON(response.getResponseBody());
            }
        });
        
        try {
            return f.get();
        } catch (InterruptedException ex) {
            throw new RouteUnavailableException("Interrupted request: "+ex.getMessage());
        } catch (ExecutionException ex) {
            throw new RouteUnavailableException(ex.getCause().getCause().getMessage());
        }
    }
    
    /**
     * Calls the WAZE api synchronously.
     * Request broadcasters of logged in user (id needed to request trafficdata of a certain place)
     * loggedIn set to false when status 403, 401
     * Sends response to parseBidFromJSON which saves the bid
     * 
     * @return boolean: true when bid found
     */ 
    private boolean getBroadcasters() throws NotAuthorizedException{
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/row-WAS/app/broadcasters/get");
        this.setHeaders(request);
        
        Future<Boolean> f = request.execute(
           new AsyncCompletionHandler<Boolean>(){
            @Override
            public Boolean onCompleted(Response response) throws Exception{
                if (response.getStatusCode() == 403 || response.getStatusCode() == 401){
                    loggedIn = false;
                    throw new NotAuthorizedException();
                }
                return parseBidFromJSON(response.getResponseBody());
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            // TODO: dit loggen
            return false;
        } catch (ExecutionException ex) {
            // TODO: dit loggen
            return false;
        }
    }
    /**
     * Calls the WAZE api synchronously.
     * Sign in to the Waze api and saves the generated tokens for the next requests.
     * Sets loggedIn = true when logged in.
     * Sets loggedIn = false if 401 / 403 received
     * 
     * @return boolean: true if succeeded
     */ 
    private boolean createLogin() throws NotAuthorizedException{
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.preparePost("https://www.waze.com/login/create");
        this.setHeaders(request);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.setBody("user_id="+USERNAME+"&password="+PASSWORD);

        Request q = request.build();
        
        Future<Boolean> f = request.execute(
           new AsyncCompletionHandler<Boolean>(){

            @Override
            public Boolean onCompleted(Response response) throws Exception{
                if (response.getStatusCode() == 200){
                    saveTokens(response);
                    loggedIn = true;
                    return true;
                }
                else if (response.getStatusCode() == 403 || response.getStatusCode() == 401){
                    loggedIn = false;
                    throw new NotAuthorizedException();
                }
                return false;
            }
        });
        try {
            return f.get();
        } catch (InterruptedException ex) {
            // TODO: loggen
            return false;
        } catch (ExecutionException ex) {
            // TODO: loggen
            return false;
        }
    }
    
    /**
     * Calls the WAZE api synchronously. 
     * Destroy current tokens = sign out
     * Removes token cookies
     * 
     * @return succeeded
     */
    private boolean destroyLogin(){
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.preparePost("https://www.waze.com/login/destroy");
        this.setHeaders(request);

        Future<Boolean> f = request.execute(
                new AsyncCompletionHandler<Boolean>(){
                    @Override
                    public Boolean onCompleted(Response response) throws Exception{
                        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300){
                            return true;
                        }
                        return false;
                    }
                });
        try {
            if (f.get()){
                this.resetCookies();
                return true;
            }
        } catch (InterruptedException ex) {
            // TODO: loggen
        } catch (ExecutionException ex) {
            // TODO: loggen
        }
        return false;
    }
        
    @Override
    public void triggerUpdate(){
        buzyRequests = new ArrayList<>();
        
        // Voor Waze moeten er verschillende calls na elkaar uitgevoerd worden. 
        // Om dit asyncrhoon te houden maak ik hier een thread aan. En die sla ik op in buzyRequests.
       Callable<Boolean> task = () -> {
            return callApi();
        };
       
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> future = executor.submit(task);

        buzyRequests.add(future);
    }
    /**
     * Login if needed
     * @return succeeded
     */
    private boolean loginIfNeeded(){
        if (!areCookiesSet()){
            if (!this.getLogin()){
                return false;
            }
        }
        if (!loggedIn){
            try {
                if (!this.createLogin()){
                    return false;
                }
            } catch (NotAuthorizedException ex) {
                // TODO: loggen
                this.destroyLogin();
                return false;
            }
        }
        return true;
    }
    /**
     * Executed in thread. Gets data and saves it to the database (same as triggerUpdate)
     * @return succeeded
     */
    private boolean callApi() throws RouteUnavailableException {
        // Structuur zodanig om aantal requests te beperken
        if (bid == 0){
            loginIfNeeded();
            try {
                if (!getBroadcasters()){
                    return false;
                }
            } catch (NotAuthorizedException ex) {
                // TODO: loggen
                
                // Try to retry the next time
                this.destroyLogin();
                return false;
            }
        }
        
        // Data ophalen
         List<DataEntry> entries;
        try {
            entries = this.getData();
        } catch (NotAuthorizedException ex) {
            // Opnieuw proberen inloggen en nog eens proberen
            this.destroyLogin();
            this.loginIfNeeded();
            try {
                entries = this.getData();
            } catch (NotAuthorizedException ex1) {
                // Ook 2e poging is mislukt
                this.destroyLogin();
                return false;
            }
        }
        for (DataEntry entry : entries){
            this.dbConnector.insert(entry);
        }
        return true;
    }
    public List<DataEntry> fetchDataFromJSON(String json) throws RouteUnavailableException {
        try{
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            List<DataEntry> entries = new ArrayList<>();
            List<Object> routes = (List<Object>) map.get("routes");
            
            Map<Integer, RouteEntry> myRoutes = new TreeMap<>();
            for(Object r : routes){
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
                Map<String, Object> e = (Map<String, Object>) line.get(line.size()-1);
                double ex = (double) e.get("x");
                double ey = (double) e.get("y");
               
                RouteEntry routeEntry = getRouteFromWazeRouteId(id);
                if (routeEntry != null){
                    DataEntry entry = new DataEntry(time, routeEntry, providerEntry);
                    entries.add(entry);
                }
            }
            return entries;
        } catch (Exception ex){
            throw new RouteUnavailableException("JSON data unreadable (expected other structure)");
        }
    }
    
    public boolean parseGetLoginJSON(String json){
        try{
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            Map<String, Object> reply =  (Map<String, Object>) map.get("reply");
            long userid = (long) reply.get("user_id");
            this.loggedIn = (boolean) reply.get("login");
            String message = (String) reply.get("message");
            long error = (long) reply.get("user_id");
            long rank = (long) reply.get("user_id");
            String full_name = (String) reply.get("full_name"); 
            return true;
        } catch (Exception ex2){
            // TODO: loggen
        }
        return false;
    }
    
    public boolean parseBidFromJSON(String json){
        try{
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            List<Object> broadcasters = (List<Object>)genson.deserialize(json, List.class);
            Object b = broadcasters.get(0);
            Map<String, Object> broadcaster = (Map<String, Object>) b;
            String name = (String) broadcaster.get("name"); // Verkeerscentrum Gent
            String env = (String) broadcaster.get("env"); // world
            int id = toIntExact((long) broadcaster.get("id"));
            this.bid = id;
            return true;
        } catch (Exception ex2){
            // TODO: loggen
        }
        return false;
    }
}
