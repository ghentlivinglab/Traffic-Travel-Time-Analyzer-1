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
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author Simon
 */
public class HereProviderConnector extends AProviderConnector {

    /**
     * List of all Future instances from last triggerUpdate (check
     * java.util.concurrent library). ± sort of threads Use f.get(); to wait the
     * thread to finish and return its value Future<ReturnType>
     * Used in the test classes to wait for the threads to finish and return its
     * DataEntry (or null if failed)
     */
    protected List<Future<DataEntry>> buzyRequests;

    public HereProviderConnector(IDbConnector dbConnector) {
        super(dbConnector, "Here");
        buzyRequests = new ArrayList<>();
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        updateInterval = Integer.parseInt(prop.getProperty("HERE_UPDATE_INTERVAL"));
    }

    @Override
    public void triggerUpdate(AsyncHttpClient a) {
        if (updateCounter % updateInterval == 0) {
            buzyRequests = new ArrayList<>();
            for (RouteEntry route : routes) {
                String url = generateURL(route);
                
                IDbConnector connector = this.dbConnector;

                Future<DataEntry> f = a.prepareGet(url).execute(
                        new AsyncCompletionHandler<DataEntry>() {

                    @Override
                    public DataEntry onCompleted(Response response) throws Exception {
                        // 200 OK Statuscode
                        if (response.getStatusCode() == 200) {
                            DataEntry data = fetchDataFromJSON(response.getResponseBody(), route);
                            connector.insert(data);
                            return data;
                        }

                        String msg = fetchErrorFromJSON(response.getResponseBody());
                        throw new RouteUnavailableException(providerName,msg);
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        // Something wrong happened.
                    }
                });
                buzyRequests.add(f);
            }
        }
        updateCounter++;
    }

    public DataEntry fetchDataFromJSON(String json, RouteEntry traject) throws RouteUnavailableException {

        Genson genson = new Genson();
        Map<String, Object> map = genson.deserialize(json, Map.class);

        Map<String, Object> response = (Map<String, Object>) map.get("response");
        List<Object> route = (List<Object>) response.get("route");
        Map<String, Object> route0 = (Map<String, Object>) route.get(0);
        Map<String, Object> summary = (Map<String, Object>) route0.get("summary");
        int travelTime = toIntExact((long) summary.get("trafficTime"));

        return new DataEntry(travelTime, traject, this.providerEntry);

    }

    public String fetchErrorFromJSON(String json) {
        try {
            // Try to read a HERE error. (HERE specific error structure)
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            String type = (String) map.get("type");
            String subtype = (String) map.get("subtype");
            String details = (String) map.get("details");
            return type + " (" + subtype + "): " + details;
        } catch (Exception ex2) {
            // Not expected ERROR JSON data
            return "JSON ERROR data unreadable (expected other structure) " + json;
        }
    }

    protected String generateURL(RouteEntry traject) {
        StringBuilder urlBuilder = new StringBuilder(prop.getProperty("HERE_API_URL"));
        urlBuilder.append("?app_code=");
        urlBuilder.append(prop.getProperty("HERE_API_CODE"));
        urlBuilder.append("&app_id=");
        urlBuilder.append(prop.getProperty("HERE_API_ID"));
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
