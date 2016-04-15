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

    @Override
    public void triggerUpdate() {
        if (updateCounter % updateInterval == 0) {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()){
                for (RouteEntry route : routes) {
                    HttpGet request = new HttpGet(generateURL(route));
                    try (CloseableHttpResponse response = client.execute(request)) {
                        if (response.getStatusLine().getStatusCode() == 200) { // Alles OK
                            HttpEntity httpEntity = response.getEntity();
                            BufferedReader rd = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                            StringBuilder json = new StringBuilder();
                            String line;
                            while ((line = rd.readLine()) != null) {
                                json.append(line);
                            }
                            
                            try{
                                DataEntry data = fetchDataFromJSON(json.toString(), route);
                                dbConnector.insert(data);
                            }catch(RouteUnavailableException e){
                                String msg = fetchErrorFromJSON(json.toString()) + " --> Route ["+ route.getId()+"]: "+ route.getName();
                                log.error(msg);
                            }

                            EntityUtils.consume(response.getEntity());
                        } else {
                            throw new RouteUnavailableException(providerName, "Something went wrong. Statuscode: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                        }
                    } catch (IOException | RouteUnavailableException ex) {
                        log.error(ex.getClass().getName() + " triggerUpdate() -> " + route.getName());
                    }
                }
            }catch(IOException e){
                log.error(e);
            }
        }
        updateCounter++;
        
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
