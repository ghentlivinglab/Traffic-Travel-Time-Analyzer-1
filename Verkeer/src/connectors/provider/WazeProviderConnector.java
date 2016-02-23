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
    protected final static String API_ID = "Oi5A1NSbw7OiI5aehvYR"; // Simon-Key
    protected final static String API_CODE = "P9NBNrOmdjYg25hufPPb9Q"; // Simon-Key
    protected Cookie csrf;
    protected Cookie web_session;
    
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
                System.out.println(response.getResponseBody());
                
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
        request.setBody("user_id=VerkeerGent&password=Paswoord1");

        Request q = request.build();
        
        Future<Response> f = request.execute(
           new AsyncCompletionHandler<Response>(){

            @Override
            public Response onCompleted(Response response) throws Exception{
                System.out.println("CREATE LOGIN RESPONSE: "+response.getStatusCode());
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
            Response r = this.getLogin();
            saveTokens(r);
            r = this.createLogin();
            saveTokens(r);
            r = this.getLogin();
            saveTokens(r);
            this.getData();
            //buzyRequests.add(f);
        } catch (InterruptedException ex) {
            Logger.getLogger(WazeProviderConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(WazeProviderConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public DataEntry fetchDataFromJSON(String json, RouteEntry traject) throws TrajectUnavailableException {
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
            throw new TrajectUnavailableException("JSON data unreadable (expected other structure)");
        }
    }
    
    public String fetchErrorFromJSON(String json){
        try{
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            String type = (String) map.get("type");
            String subtype = (String) map.get("subtype");
            String details = (String) map.get("details");
            return type+" ("+subtype+"): "+details;
        } catch (Exception ex2){
            // Not expected ERROR JSON data
            return "JSON ERROR data unreadable (expected other structure)";
        }
    }
    
    protected String generateURL(RouteEntry traject) {
        StringBuilder urlBuilder = new StringBuilder(API_URL);
        urlBuilder.append("?app_code=");
        urlBuilder.append(API_CODE);
        urlBuilder.append("&app_id=");
        urlBuilder.append(API_ID);
        urlBuilder.append("&waypoint0=");
        urlBuilder.append(traject.getStartCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(traject.getStartCoordinateLongitude());
        urlBuilder.append("&waypoint1=");
        urlBuilder.append(traject.getEndCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(traject.getEndCoordinateLongitude());
        urlBuilder.append("&mode=shortest;car;traffic:enabled");
        urlBuilder.append("&departure=now");
        return urlBuilder.toString();
    }
}