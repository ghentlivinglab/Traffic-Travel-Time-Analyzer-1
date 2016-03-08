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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 *
 * @author jarno
 */
public class GoogleProviderConnector extends AProviderConnector {
    
    /**
     * List of all Future instances from last triggerUpdate (check
     * java.util.concurrent library). ± sort of threads Use f.get(); to wait the
     * thread to finish and return its value Future<ReturnType>
     * Used in the test classes to wait for the threads to finish and return its
     * DataEntry (or null if failed)
     */
    
    protected List<Future<DataEntry>> buzyRequests;
      
    /**
     * Constructs a new GoogleProviderConnector with an IDbConnector to write
     * data to storage
     *
     * @param dbConnector connector to write DataEntry to
     */
    public GoogleProviderConnector(IDbConnector dbConnector) {
        super(dbConnector);
        String providerName = "Google Maps";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName); // gets the provider-information from the database
        updateInterval = Integer.parseInt(prop.getProperty("GOOGLE_UPDATE_INTERVAL"));
    }

    @Override
    public void triggerUpdate() {
        if(updateCounter%updateInterval == 0){
            buzyRequests = new ArrayList<>();
            for (RouteEntry route : routes) {
                String url = generateURL(route);
                AsyncHttpClient asyncHttpClient;
                asyncHttpClient = new AsyncHttpClient(); // create http client

                IDbConnector connector = this.dbConnector;

                Future<DataEntry> f = asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<DataEntry>() {
                    @Override
                    public DataEntry onCompleted(Response response) throws Exception {
                        if (response.getStatusCode() == 200) {// status == OK
                            Map<String, Object> rawData = fetchDataFromJSON(response.getResponseBody());
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
                buzyRequests.add(f);
            }
        }
        updateCounter++;
    }

    /**
     * Generates the URL to call the Google API for this route.
     *
     * @param route current route for which to get the information
     * @return String that contains the URL to get info about current route
     */
    protected String generateURL(RouteEntry route) {
        StringBuilder urlBuilder = new StringBuilder(prop.getProperty("GOOGLE_API_URL"));
        urlBuilder.append("?key=");
        urlBuilder.append(prop.getProperty("GOOGLE_API_KEY"));
        urlBuilder.append("&origin=");
        urlBuilder.append(route.getStartCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(route.getStartCoordinateLongitude());
        urlBuilder.append("&destination=");
        urlBuilder.append(route.getEndCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(route.getEndCoordinateLongitude());

        return urlBuilder.toString();

    }

    /**
     * Extracts the data from the JSON and generates a Map with all the info
     * from it.
     * <p>
 If dataset is invalid, this wil throw a RouteUnavailableException
     *
     * @param json String which contains the JSON-object
     * @throws RouteUnavailableException if data is not valid
     * @return Google-dataset about current route
     */
    protected Map<String, Object> fetchDataFromJSON(String json) throws RouteUnavailableException {
        Genson genson = new Genson();
        Map<String, Object> map = genson.deserialize(json, Map.class);
        if (!map.get("status").equals("OK")) { // google data is not correct
            throw new RouteUnavailableException((String) map.get("status"));
        }
        return map;
    }

    /**
     * Processes the raw JSON-data into a DataEntry
     *
     * @param route RouteEntry that contains the information about the current
     * route
     * @param rawData Map&lt;String,Object&gt; that contains the raw JSON-data
     * @return Entry that contains all the data about current route
     */
    protected DataEntry processData(RouteEntry route, Map<String, Object> rawData) {
        DataEntry data = new DataEntry(-1, route, providerEntry);
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
