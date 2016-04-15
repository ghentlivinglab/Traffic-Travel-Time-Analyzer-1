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
import static java.lang.Math.toIntExact;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
 * @author Robin
 */
public class TomTomProviderConnector extends AProviderConnector {

    protected List<Future<DataEntry>> buzyRequests;
    private static final Logger log = Logger.getLogger(TomTomProviderConnector.class);
    private int APIKeys;
    private int alternator=0;

    public TomTomProviderConnector(IDbConnector dbConnector) {
        super(dbConnector,"TomTom");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        updateInterval = Integer.parseInt(prop.getProperty("TOMTOM_UPDATE_INTERVAL"));
        APIKeys = Integer.parseInt(prop.getProperty("TOMTOM_API_KEYS"));
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

    public String fetchErrorFromJSON(String json) {
        try {
            // Try to read a TOMTOM error. (TOMTOM specific error structure)
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);
            Map<String, Object> error = (Map<String, Object>) map.get("error");
            String description = (String) error.get("description");
            
            return description;
        } catch (Exception ex2) {
            // Not expected ERROR JSON data
            return "JSON ERROR data unreadable (expected other structure) " + json;
        }
    }
    
    public DataEntry fetchDataFromJSON(String json, RouteEntry traject) throws RouteUnavailableException {
        try {
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);

            List<Object> route = (List<Object>) map.get("routes");
            Map<String, Object> route0 = (Map<String, Object>) route.get(0);
            Map<String, Object> summary = (Map<String, Object>) route0.get("summary");
            int travelTime = toIntExact((long) summary.get("travelTimeInSeconds"));

            return new DataEntry(travelTime, traject, this.providerEntry);
        } catch (Exception ex) {
            throw new RouteUnavailableException(providerName,"JSON data unreadable (expected other structure)");
        }
    }

    protected String generateURL(RouteEntry traject) {
        StringBuilder urlBuilder = new StringBuilder(prop.getProperty("TOMTOM_API_URL1"));
        urlBuilder.append(traject.getStartCoordinateLatitude());
        urlBuilder.append(',');
        urlBuilder.append(traject.getStartCoordinateLongitude());
        urlBuilder.append(':');
        urlBuilder.append(traject.getEndCoordinateLatitude());
        urlBuilder.append(',');
        urlBuilder.append(traject.getEndCoordinateLongitude());
        urlBuilder.append(prop.getProperty("TOMTOM_API_URL2"));
        urlBuilder.append(getAPIKey());
        return urlBuilder.toString();
    }

    private String getAPIKey(){
        String ret = prop.getProperty("TOMTOM_API_KEY"+alternator);
        alternator++;
        alternator%=APIKeys;
        return ret;
    }
}
