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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.List;
import java.util.Map;

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

    public GoogleProviderConnector(List<RouteEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        String providerName = "Google Maps";
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
    }

    @Override
    public void triggerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet.");
        for (RouteEntry route : trajecten) {
            try {
                URL url = generateURL(route);
                URLConnection connection = url.openConnection();
                connection.connect();
                Map<String, Object> rawData = fetchDataFromJSON(connection.getInputStream());
                DataEntry data = processData(route, rawData);
                dbConnector.insert(data);
            } catch (TrajectUnavailableException e) { // exception when returned data is invalid
                // TODO exception handling
            } catch (MalformedURLException e) { // exception when url invalid

            } catch (IOException e) { // exception when connection failed
            }

        }
    }

    /**
     * Generates the URL to call the Google API for this route.
     *
     * @param traject
     * @return
     * @throws MalformedURLException
     */
    protected URL generateURL(RouteEntry traject) throws MalformedURLException {
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

        URL url = new URL(urlBuilder.toString());

        return url;

    }

    /**
     *
     * @param json
     * @throws TrajectUnavailableException if data is invalid
     * @return Map<String,Object> Google-dataset about current route
     */
    protected Map<String, Object> fetchDataFromJSON(InputStream json) throws TrajectUnavailableException {
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
