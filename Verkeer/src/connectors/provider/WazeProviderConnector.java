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
import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
    protected boolean loggedIn = false;
    
    protected final static int BID = 147; // Hardcoded momenteel

    /**
     * List of all Future instances from last triggerUpdate (check java.util.concurrent library). 
     * ± sort of threads
     * Use f.get(); to wait the thread to finish and return its value Future<ReturnType>
     * Used in the test classes to wait for the threads to finish and return its DataEntry (or null if failed)
     */
    protected List<Future<DataEntry>> buzyRequests;
            
    public WazeProviderConnector(IDbConnector dbConnector) {
        super(dbConnector);
        buzyRequests = new ArrayList<>();
        String providerName = "Waze";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
    }
    public void setHeaders(BoundRequestBuilder request){
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Referer", "https://www.waze.com/en/signin?redirect=/trafficview");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.addHeader("Accept-Language", "nl-NL,nl;q=0.8,en-US;q=0.6,en;q=0.4");
        request.addHeader("Origin", "https://www.waze.com");

        if (csrf != null && web_session != null){
            request.setHeader("X-CSRF-Token", csrf.getValue());
            request.addCookie(csrf);
            request.addCookie(web_session);
        }
        request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        request.setHeader("Accept", "*/*");
    }
    public void saveTokens(Response response){
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
    public Response getLogin() throws InterruptedException, ExecutionException{
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
    public Response getData() throws InterruptedException, ExecutionException{
        //https://www.waze.com/login/get
        AsyncHttpClient asyncHttpClient;
        asyncHttpClient = new AsyncHttpClient();

        BoundRequestBuilder request = asyncHttpClient.prepareGet("https://www.waze.com/row-rtserver/broadcast/BroadcastRSS?bid="+BID+"&format=JSON");
        this.setHeaders(request);
        
        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("GET DATA RESPONSE: "+response.getStatusCode());
                System.out.println(response.getResponseBody());
                
                return response;
            }
        });
        return f.get();
    }
    public Response createLogin() throws InterruptedException, ExecutionException{
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
                saveTokens(response);

                return response;
            }

            @Override
            public void onThrowable(Throwable t){
                // Something wrong happened.
            }
        });
        return f.get();
    }
    @Override
    public void triggerUpdate(){
        buzyRequests = new ArrayList<>();
        try {
            this.getLogin();
            if (!this.loggedIn){
                this.createLogin();
            }
            this.getLogin();
            if (!this.loggedIn){
                System.out.println("Failed login!");
                return;
            }
            
            this.getData();
            //buzyRequests.add(f);
        } catch (InterruptedException ex) {
            Logger.getLogger(WazeProviderConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(WazeProviderConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public DataEntry fetchDataFromJSON(String json, RouteEntry traject) throws RouteUnavailableException {
        try{
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            
            Map<String, Object> response = (Map<String, Object>) map.get("response");
            List<Object> route = (List<Object>) response.get("route");
            Map<String, Object> route0 = (Map<String, Object>) route.get(0);
            Map<String, Object> summary = (Map<String, Object>) route0.get("summary");
            int travelTime = toIntExact((long) summary.get("trafficTime"));
                    
            return new DataEntry(travelTime, traject, this.providerEntry);
        } catch (Exception ex){
            throw new RouteUnavailableException("JSON data unreadable (expected other structure)");
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
    
}