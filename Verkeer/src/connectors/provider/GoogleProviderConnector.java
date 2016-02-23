/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.owlike.genson.Genson;
import connectors.DataEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author jarno
 */
public class GoogleProviderConnector extends AProviderConnector {

    protected final static String API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    protected final static String API_KEY = "AIzaSyCD3ESlkpUJGJvRKrJguvBa25eFNIJrujo"; // Jarno-Key
    //     private final static String API_KEY = ; // Piet-Key
    //     private final static String API_KEY = ; // Robin-Key
    //     private final static String API_KEY = ; // Simon-Key

    /**
     * List of all Future instances from last triggerUpdate (check
     * java.util.concurrent library). ± sort of threads Use f.get(); to wait the
     * thread to finish and return its value Future<ReturnType>
     * Used in the test classes to wait for the threads to finish and return its
     * DataEntry (or null if failed)
     */
    protected List<Future<DataEntry>> buzyRequests;

    public GoogleProviderConnector(List<RouteEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        String providerName = "Google Maps";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
    }

    @Override
    public void triggerUpdate() {
        for (RouteEntry route : trajecten) {
            String url = generateURL(route);
            AsyncHttpClient asyncHttpClient;
            asyncHttpClient = new AsyncHttpClient();

            IDbConnector connector = this.dbConnector;

            Future<DataEntry> f = asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<DataEntry>() {
                @Override
                public DataEntry onCompleted(Response response) throws Exception {
                    if(response.getStatusCode()== 200){
                        Map<String,Object> rawData = fetchDataFromJSON(response.getResponseBody());
                        DataEntry data = processData(route, rawData);
                        connector.insert(data);
                        return data;
                    }
                    // Er ging iets fout
                    // TODO: Statuscodes later uitbreiden met: 
                    // https://developer.here.com/rest-apis/documentation/traffic/topics/http-status-codes.html
                    throw new Exception();
                }
            });
        }
    }

    /**
     * Generates the URL to call the Google API for this route.
     *
     * @param traject
     * @return
     * @throws MalformedURLException
     */
    protected String generateURL(RouteEntry traject) {
        StringBuilder urlBuilder = new StringBuilder(API_URL);
        urlBuilder.append("?key=");
        urlBuilder.append(API_KEY);
        urlBuilder.append("&origin=");
        urlBuilder.append(traject.getStartCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(traject.getStartCoordinateLongitude());
        urlBuilder.append("&destination=");
        urlBuilder.append(traject.getEndCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(traject.getEndCoordinateLongitude());

        return urlBuilder.toString();

    }

    /**
     *
     * @param json
     * @throws TrajectUnavailableException if data is invalid
     * @return Map<String,Object> Google-dataset about current route
     */
    protected Map<String, Object> fetchDataFromJSON(String json) throws TrajectUnavailableException {
        Genson genson = new Genson();
        Map<String, Object> map = genson.deserialize(json, Map.class);
        if (!map.get("status").equals("OK")) { // google data is not correct
            throw new TrajectUnavailableException((String) map.get("status"));
        }
        return map;
    }

    /**
     *
     * @param route
     * @param rawData
     * @return
     */
    protected DataEntry processData(RouteEntry route, Map<String, Object> rawData) {
        DataEntry data = new DataEntry(new Date(System.currentTimeMillis()), -1, route, providerEntry);
        // get all routes
        List<Object> routes = (List<Object>) rawData.get("routes");

        for (int i = 0; i < routes.size(); i++) { // for each found route
            int distance = 0;
            int duration = 0;
            // get all legs from route-object
            List<Object> legs = (List<Object>) ((Map<String, Object>) routes.get(i)).get("legs");
            for (int j = 0; j < legs.size(); j++) { // for each leg of a route
                // get distance object from leg
                Map<String, Object> distanceObject = (Map<String, Object>) ((Map<String, Object>) legs.get(j)).get("distance");
                // get duration object from leg
                Map<String, Object> durationObject = (Map<String, Object>) ((Map<String, Object>) legs.get(j)).get("duration");

                // calculate route distance and duration
                distance += Math.toIntExact((long) distanceObject.get("value"));
                duration += Math.toIntExact((long) durationObject.get("value"));
            }
            if (true) { // TODO add condition to check if route is correct one (probably via distance or shortest route)
                data.setTravelTime(duration);
            }
        }
        return data;
    }
}
