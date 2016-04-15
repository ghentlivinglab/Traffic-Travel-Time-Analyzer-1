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

    @Override
    public void triggerUpdate() {
        if (updateCounter % updateInterval == 0) {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()){
                for (RouteEntry route : routes) {
                    log.info(route.getName());
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
                            DataEntry data = fetchDataFromJSON(json.toString(), route);
                            dbConnector.insert(data);

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
