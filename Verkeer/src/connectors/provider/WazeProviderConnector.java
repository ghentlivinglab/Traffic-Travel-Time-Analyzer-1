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
    
    private boolean areCookiesSet(){
        return csrf != null && web_session != null;
    }
    private void setHeaders(BoundRequestBuilder request){
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Referer", "https://www.waze.com/en/signin?redirect=/trafficview");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.addHeader("Accept-Language", "nl-NL,nl;q=0.8,en-US;q=0.6,en;q=0.4");
        request.addHeader("Origin", "https://www.waze.com");

        if (this.areCookiesSet()){
            request.setHeader("X-CSRF-Token", csrf.getValue());
            request.addCookie(csrf);
            request.addCookie(web_session);
        }
        //request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        request.setHeader("Accept", "*/*");
    }
    
    /**
     * Maps wazeRouteId to get RouteEntries from the DbConnector
     */
    private RouteEntry getRouteFromWazeRouteId(int wazeRouteId){
        // TODO: ID mapping implementeren op één of andere magische manier
        if (wazeRouteId < 4069 || wazeRouteId > 4098){
            return null;
        }
        return dbConnector.findRouteEntryByID(wazeRouteId-4068);
    }
    
    /**
     * Haalt set-cookie headers uit het Response. En slaat deze op voor de volgende request.
     */
    private void saveTokens(Response response){
        List<Cookie> cookies = response.getCookies();
        for (Cookie cookie : cookies){
            if (cookie.getName().equals("_csrf_token")){
                csrf = cookie;
            }
            else if (cookie.getName().equals("_web_session")){
                web_session = cookie;
            }else{
                System.out.println("Onverwachte cookie gevonden - "+cookie.getName()+": "+cookie.getValue());
            }
        }
    }
    
    /**
     * Calls the WAZE api. Asks if the current tokens (cookies) are authorized.
     * Result contains JSON with loggedIn, user_id, message field, ...
     */
    private Response getLogin() throws InterruptedException, ExecutionException{
        //https://www.waze.com/login/get
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/login/get");
        this.setHeaders(request);
        

        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("GET LOGIN RESPONSE: "+response.getStatusCode());
                saveTokens(response);
                parseGetLoginJSON(response.getResponseBody());
                
                return response;
            }
        });
        return f.get();
    }
    
    /**
     * Calls the WAZE api. 
     * Returns JSON with all the needed data (traveltime, ...)
     * Status 403 if not authorized
     */
    private Response getData() throws InterruptedException, ExecutionException{
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/row-rtserver/broadcast/BroadcastRSS?bid="+bid+"&format=JSON");
        this.setHeaders(request);
        
        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("GET DATA RESPONSE: "+response.getStatusCode());
                if (response.getStatusCode() == 403){
                    loggedIn = false;
                }
                return response;
            }
        });
        return f.get();
    }
    
    /**
     * Get the correct BID, and save it (broadcastId)
     * Returns 404 when not logged in
     */
    
    private Response getBroadcasters() throws InterruptedException, ExecutionException{
        //https://www.waze.com/row-WAS/app/broadcasters/get
        // [{"name":"Verkeerscentrum Gent","id":147,"env":"world"}]
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/row-WAS/app/broadcasters/get");
        this.setHeaders(request);
        
        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("GET BROADCASTERS RESPONSE: "+response.getStatusCode());
                parseBidFromJSON(response.getResponseBody());
                if (response.getStatusCode() == 403){
                    loggedIn = false;
                }
                return response;
            }
        });
        return f.get();
    }
    
    /**
     * Calls the WAZE api. Sign in to the Waze api and saves the generated tokens for the next requests.
     */
    private Response createLogin() throws InterruptedException, ExecutionException{
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.preparePost("https://www.waze.com/login/create");
        this.setHeaders(request);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.setBody("user_id="+USERNAME+"&password="+PASSWORD);

        Request q = request.build();
        
        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){

            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("CREATE LOGIN RESPONSE: "+response.getStatusCode());
                if (response.getStatusCode() == 200){
                    saveTokens(response);
                    loggedIn = true;
                }
                if (response.getStatusCode() == 403){
                    loggedIn = false;
                }
                return response;
            }

            @Override
            public void onThrowable(Throwable t){
                // Something wrong happened.
            }
        });
        return f.get();
    }
    
    /**
     * Calls the WAZE api. Destroy current tokens = sign out
     * Status 204 = succeeded
     */
    private Response destroyLogin() throws InterruptedException, ExecutionException{
        //https://www.waze.com/login/get
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.preparePost("https://www.waze.com/login/destroy");
        this.setHeaders(request);
        

        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("DESTROY LOGIN RESPONSE: "+response.getStatusCode());
                return response;
            }
        });
        return f.get();
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
    public boolean callApi() throws InterruptedException, ExecutionException, TrajectUnavailableException, IOException{
        // Structuur zodanig om aantal requests te beperken
        if (bid == 0){
            if (!this.areCookiesSet()){
                this.getLogin();
            }
            if (!loggedIn){
                Response r = this.createLogin();
                if (r.getStatusCode() != 200){
                    // Mislukt: inloggen niet gelukt
                    return false;
                }
            }
            getBroadcasters();
            if (bid == 0){
                return false;
            }
        }
        Response r = this.getData();
        if (r.getStatusCode() == 403){
            r = this.createLogin();
            if (r.getStatusCode() != 200){
                // Mislukt: inloggen niet gelukt
                return false;
            }
            // Opnieuw proberen
            r = this.getData();
        }
        if (r.getStatusCode() != 200){
             System.out.println("Failed: downloading data!");
            return false;
        } 
        // Optioneel: uitloggen (om niet te veel tokens aan te maken => Waze?)
        //this.destroyLogin(); // 204 no content => geslaagd

        List<DataEntry> entries = fetchDataFromJSON(r.getResponseBody());
        for (DataEntry entry : entries){
            this.dbConnector.insert(entry);
        }
        return true;
    }
    public List<DataEntry> fetchDataFromJSON(String json) throws TrajectUnavailableException {
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
                
                /*RouteEntry d = new RouteEntry(name, sy, sx, ey, ex, length, htime);
                myRoutes.put(id, d);*/
                
                RouteEntry routeEntry = getRouteFromWazeRouteId(id);
                System.out.println(routeEntry.getId()+"\t\t\t"+routeEntry+"\t\t\t"+providerEntry.getId());
                if (routeEntry != null){
                    DataEntry entry = new DataEntry(time, routeEntry, providerEntry);
                    entries.add(entry);
                }
            }
            /*for (Map.Entry<Integer, RouteEntry> entrySet : myRoutes.entrySet()) {
                Integer key = entrySet.getKey();
                RouteEntry value = entrySet.getValue();
                
                System.out.println(key+"."+ value);
            }*/
            
            return entries;
        } catch (Exception ex){
            throw new TrajectUnavailableException("JSON data unreadable (expected other structure)");
        }
    }
    
    public void parseGetLoginJSON(String json){
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
        } catch (Exception ex2){
            // Not expected ERROR JSON data
            
            System.out.println("Unexpected JSON format from getLogin: "+ex2.getMessage()+" - "+json);
        }
    }
    
    public void parseBidFromJSON(String json){
        try{
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            List<Object> broadcasters = (List<Object>)genson.deserialize(json, List.class);
            for (Object b : broadcasters){
                Map<String, Object> broadcaster = (Map<String, Object>) b;
                String name = (String) broadcaster.get("name"); // Verkeerscentrum Gent
                String env = (String) broadcaster.get("env"); // world
                int id = toIntExact((long) broadcaster.get("id"));
                this.bid = id;
            }
        } catch (Exception ex2){
            // Not expected ERROR JSON data
            
            System.out.println("Unexpected JSON format from getLogin: "+ex2.getMessage()+" - "+json);
        }
    }
    
}