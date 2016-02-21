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
import com.owlike.genson.Genson;
import connectors.DataEntry;
import static java.lang.Math.toIntExact;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author Simon
 */
public class HereProviderConnector extends AProviderConnector {
    protected final static String API_URL = "https://route.cit.api.here.com/routing/7.2/calculateroute.json";
    protected final static String API_ID = "Oi5A1NSbw7OiI5aehvYR"; // Simon-Key
    protected final static String API_CODE = "P9NBNrOmdjYg25hufPPb9Q"; // Simon-Key

    /**
     * List of all Future instances (check java.util.concurrent library). 
     * ± sort of threads
     * Use f.get(); to wait the thread to finish and return its value Future<ReturnType>
     * Used in the test classes to wait for the threads to finish and return its DataEntry (or null if failed)
     */
    protected List<Future<DataEntry>> buzyRequests;
            
    public HereProviderConnector(List<RouteEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        buzyRequests = new ArrayList<>();
        String providerName = "Here";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
    }
    
    @Override
    public void triggerUpdate(){
        for (RouteEntry traject : trajecten) {
            String url = generateURL(traject);
            AsyncHttpClient asyncHttpClient;
            asyncHttpClient = new AsyncHttpClient();
            Future<DataEntry> f = asyncHttpClient.prepareGet(url).execute(
               new AsyncCompletionHandler<DataEntry>(){

                @Override
                public DataEntry onCompleted(Response response) throws Exception{
                    // 200 OK Statuscode
                    if (response.getStatusCode() == 200){
                        return fetchDataFromJSON(response.getResponseBody(), traject);
                    }
                    // Er ging iets fout
                    // Statuscodes later uitbreiden met: 
                    // https://developer.here.com/rest-apis/documentation/traffic/topics/http-status-codes.html
                    
                    String msg = fetchErrorFromJSON(response.getResponseBody());
                    throw new TrajectUnavailableException(msg);
                }

                @Override
                public void onThrowable(Throwable t){
                    // Something wrong happened.
                    //t.getMessage();
                }
            });
            buzyRequests.add(f);
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
