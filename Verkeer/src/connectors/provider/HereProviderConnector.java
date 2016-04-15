/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import java.util.List;
import connectors.database.IDbConnector;
import connectors.RouteEntry;
import com.owlike.genson.Genson;
import connectors.DataEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.toIntExact;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Simon
 */
public class HereProviderConnector extends AProviderConnector {

    private static final Logger log = Logger.getLogger(HereProviderConnector.class);
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
                DataEntry data = fetchDataFromJSON(response.toString(), route);
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
