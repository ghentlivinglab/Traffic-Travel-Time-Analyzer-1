/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.owlike.genson.Genson;
import connectors.DataEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 *
 * @author jarno
 */
public class GoogleProviderConnector extends AProviderConnector {

    private static final Logger log = Logger.getLogger(GoogleProviderConnector.class);
    private List<String> ApiKeys;
    private int alternator = 0;

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
        super(dbConnector, "Google Maps");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName); // gets the provider-information from the database
        updateInterval = Integer.parseInt(prop.getProperty("GOOGLE_UPDATE_INTERVAL"));
        
        int count = Integer.parseInt(prop.getProperty("GOOGLE_API_KEYS"));
        ApiKeys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ApiKeys.add(prop.getProperty("GOOGLE_API_KEY"+i));
        }
        
    }
    
    /**
     * Sets the required HTTP headers for a request to the Here API: - Disable
     * cache - Accept gzip - Set origin to waze.com - Accept language: Dutch,
     * Accept all media types
     *
     * @param request to set headers of
     */
    private void setHeaders(HttpURLConnection request) {
        request.setRequestProperty("Pragma", "no-cache");
        request.setRequestProperty("Cache-Control", "no-cache");
        request.setRequestProperty("Accept-Encoding", "deflate");
        request.setRequestProperty("Accept", "*/*");
    }
    

    public DataEntry getData(RouteEntry route) throws RouteUnavailableException {
        String url = generateURL(route);

        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException ex) {
            throw new RouteUnavailableException(providerName, "Failed getting data: Url malformed " + url);
        }

        try {
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            setHeaders(con);
            
            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            if (responseCode == 200) { // Alles OK
                Map<String, Object> rawData = fetchDataFromJSON(response.toString());
                DataEntry data = processData(route, rawData);
                return data;
            } else {
                throw new RouteUnavailableException(providerName, "Something went wrong. Statuscode: " + responseCode + " " + responseMessage);
            }
        } catch (IOException ex) {
            throw new RouteUnavailableException(providerName, "Failed getting data: IOException :" + ex.getMessage());
        }
    }

    @Override
    public void triggerUpdate() {

        for (RouteEntry route : routes) {
            try {
                DataEntry data = getData(route);
                dbConnector.insert(data);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
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
        urlBuilder.append(getAPIKey());
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
     * If dataset is invalid, this will throw a RouteUnavailableException
     *
     * @param json String which contains the JSON-object
     * @throws RouteUnavailableException if data is not valid
     * @return Google-dataset about current route
     */
    protected Map<String, Object> fetchDataFromJSON(String json) throws RouteUnavailableException {
        Genson genson = new Genson();
        Map<String, Object> map = genson.deserialize(json, Map.class
        );
        if (!map.get(
                "status").equals("OK")) { // google data is not correct
            throw new RouteUnavailableException(providerName, (String) map.get("status"));
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

    /**
     * A temporary functions that switches between API keys
     *
     * @return String an API Key
     */
    private String getAPIKey() {
        String ret = ApiKeys.get(alternator);
        alternator++;
        alternator %= ApiKeys.size();
        return ret;
    }

}
