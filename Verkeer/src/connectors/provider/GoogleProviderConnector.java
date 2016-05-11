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
import org.apache.log4j.Logger;

public class GoogleProviderConnector extends AProviderConnector {

    private static final Logger log = Logger.getLogger(GoogleProviderConnector.class);
    private List<String> ApiKeys;
    private int alternator = 0;

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
            ApiKeys.add(prop.getProperty("GOOGLE_API_KEY" + i));
        }

    }

    /**
     * Sets the required HTTP headers for a request to the Here API: - Disable
     * cache - Accept gzip - Accept all media types
     *
     * @param request to set headers of
     */
    private void setHeaders(HttpURLConnection request) {
        request.setRequestProperty("Pragma", "no-cache");
        request.setRequestProperty("Cache-Control", "no-cache");
        request.setRequestProperty("Accept-Encoding", "deflate");
        request.setRequestProperty("Accept", "*/*");
    }

    /**
     * Method that makes the connection with google and fetches the data
     *
     * @param route
     * @return the generated data
     * @throws RouteUnavailableException when an error with the connection or
     * the data occurs
     */
    public DataEntry getData(RouteEntry route) throws RouteUnavailableException {
        String url = generateURL(route);

        URL obj;
        try {
            obj = new URL(url); // generate url based on the string with the specific url
        } catch (MalformedURLException ex) {
            throw new RouteUnavailableException(providerName, "Failed getting data: Url malformed " + url);
        }

        try {
            // fetch the data from the google-servers and stores it in a buffer
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            setHeaders(con);
            // optional, default should be GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200) { // Data has been fetched from the server and is stored in the response-object
                Map<String, Object> rawData = fetchDataFromJSON(response.toString()); // translate json to java-object
                DataEntry data = processData(route, rawData); // fetch data from java object and store in data
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
        // get start address
        StringBuilder urlBuilder = new StringBuilder(prop.getProperty("GOOGLE_API_URL"));
        // get key
        urlBuilder.append("?key=");
        urlBuilder.append(getAPIKey());
        // get start point
        urlBuilder.append("&origin=");
        urlBuilder.append(route.getStartCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(route.getStartCoordinateLongitude());
        // get destination point
        urlBuilder.append("&destination=");
        urlBuilder.append(route.getEndCoordinateLatitude());
        urlBuilder.append(",");
        urlBuilder.append(route.getEndCoordinateLongitude());
        // set timing for this moment so real time data is used
        urlBuilder.append("&departure_time=now");

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
        // translation is done via Genson
        Genson genson = new Genson();
        Map<String, Object> map = genson.deserialize(json, Map.class);
        if (!map.get("status").equals("OK")) { // google data is not correct
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
     * @throws connectors.provider.RouteUnavailableException when data is not
     * valid
     */
    protected DataEntry processData(RouteEntry route, Map<String, Object> rawData) throws RouteUnavailableException {
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

            // condition to check if route is correct via a margin on the distance. 
            // this is to compensate for minor changes in the road that will occur.
            // if we don't compensate for this, we would have to correct our distance with every change Google pushes
            if (DistanceTolerated(distance, route)) {
                data.setTravelTime(duration);
                return data;
            }
        }
        // no route has a tolerated distance, hence no valid route is found
        throw new RouteUnavailableException(providerName, "No route found");

    }

    /**
     * Calculates if the given distance can be considered correct for the given
     * route.<p>
     * Calculation is based on the idea that most detours will add more than 100
     * meter to the distance. Thus this distance can be rejected when it is not
     * within this margin
     *
     * @param distance integer which contains distance in meter
     * @param route RouteEntry to which the distance should be checked
     * @return true when tolerated, false when not
     */
    private boolean DistanceTolerated(int distance, RouteEntry route) {
        int correctDistance = route.getLength();
        int correctionDistance = 100; // difference can be maximum 100 meter
        int maxDistance = correctDistance + correctionDistance;
        int minDistance = correctDistance - correctionDistance;

        if (minDistance < distance && distance < maxDistance) { // the distance may vary anywhere between -100 and 100 meter from the route length to be considered correct
            return true;
        }
        return false;
    }

    /**
     * A functions that switches between API keys
     * this because of limitations of free accounts
     * @return String an API Key
     */
    private String getAPIKey() {
        String ret = ApiKeys.get(alternator);
        alternator++;
        alternator %= ApiKeys.size();
        return ret;
    }

}
