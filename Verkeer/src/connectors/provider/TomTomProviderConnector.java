/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.ning.http.client.*;
import com.owlike.genson.Genson;
import connectors.DataEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import static java.lang.Math.toIntExact;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
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

                        String msg = fetchErrorFromJSON(response.getResponseBody()) + " --> Route ["+ route.getId()+"]: "+ route.getName();
                        throw new RouteUnavailableException(providerName,msg);
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        // Something wrong happened.
                    }
                });
                buzyRequests.add(f);
                try {
                    sleep(300);
                } catch (InterruptedException ex) {
                    log.fatal(ex);
                }
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
