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
import java.util.TreeMap;
import java.util.concurrent.Future;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

/**
 *
 * @author Simon
 */
public class WazeProviderConnector extends AProviderConnector {
    private int bid = 147;
    private static final Logger log = Logger.getLogger(WazeProviderConnector.class);

    /**
     * List of all Future instances from last triggerUpdate (check
     * java.util.concurrent library). ± sort of threads Use f.get(); to wait the
     * thread to finish and return its value Future<ReturnType>
     * Return value = Boolean => succeeded = true
     */
    protected List<Future<Boolean>> buzyRequests;

    public WazeProviderConnector(IDbConnector dbConnector) {
        super(dbConnector, "Waze");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        updateInterval = Integer.parseInt(prop.getProperty("WAZE_UPDATE_INTERVAL"));
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
        request.setRequestProperty("Accept-Encoding", "gzip, deflate");
        request.setRequestProperty("Accept", "*/*");
    }

    /**
     * Maps wazeRouteId on RouteEntries from the DbConnector
     *
     * @param wazeRouteId searched for
     * @return RouteEntry connected to this wazeRouteId
     */
    private RouteEntry getRouteFromWazeRouteId(int wazeRouteId) {
        if (wazeRouteId < 4069 || wazeRouteId > 4098) {
            return null;
        }
        return dbConnector.findRouteEntryByID(wazeRouteId - 4068);
    }

    /**
     * Calls the WAZE api synchronously. Request all traffic data in JSON and
     * returns them parsed into DataEntries. Doesn't need tokens (weird). If the
     * response returns a 403, 401 status code, loggedIn will be set to false.
     * If not okay (200): throws RouteUnavailableException.
     *
     * @return List<DataEntry> parsed from JSON response from WAZE api.
     */
    private List<DataEntry> getData() throws RouteUnavailableException, NotAuthorizedException {
        String url = prop.getProperty("WAZE_API_URL_GET_DATA") + bid;

        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException ex) {
            throw new RouteUnavailableException(providerName, "Failed getting data: Url malformed "+url);
        }
            
        try {
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            
            // optional default is GET
            con.setRequestMethod("GET");
            
            //add request header
            this.setHeaders(con);
            
            int responseCode = con.getResponseCode();
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            //print result
            
            List<DataEntry> ret = new ArrayList<>();
            if (responseCode == 403 || responseCode == 401) {
                throw new NotAuthorizedException("Authentication failed: Status " + responseCode);
            }
            
            if (responseCode != 200) {
                throw new RouteUnavailableException(providerName, "Failed getting data from Waze: Status "+responseCode);
            }
            
            ret = fetchDataFromJSON(response.toString());
            return ret;
        } catch (IOException ex) {
            throw new RouteUnavailableException(providerName, "Failed getting data from Waze: IOException :"+ex.getMessage());
        }
    }

    @Override
    public void triggerUpdate() {
        try {
            // Data ophalen
            List<DataEntry> entries = new ArrayList<>();
            entries = getData();

            for (DataEntry entry : entries) {
                this.dbConnector.insert(entry);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public List<DataEntry> fetchDataFromJSON(String json) throws RouteUnavailableException {
        List<DataEntry> entries = new ArrayList<>();
        try {
            Genson genson = new Genson();
            Map<String, Object> map = genson.deserialize(json, Map.class);

            List<Object> routes = (List<Object>) map.get("routes");

            Map<Integer, RouteEntry> myRoutes = new TreeMap<>();
            for (Object r : routes) {
                Map<String, Object> route = (Map<String, Object>) r;
                // Hier haal ik nog extra optionele data op die we niet meteen gebruiken
                // Kan handig zijn voor later
                int time = toIntExact((long) route.get("time"));
                int htime = toIntExact((long) route.get("historicTime"));
                int id = toIntExact((long) route.get("id"));
                int jamlevel = toIntExact((long) route.get("jamLevel"));
                int length = toIntExact((long) route.get("length"));
                String name = (String) route.get("name");
                String toName = (String) route.get("toName");
                String fromName = (String) route.get("fromName");

                List<Object> line = (List<Object>) route.get("line");
                Map<String, Object> s = (Map<String, Object>) line.get(0);
                double sx = (double) s.get("x");
                double sy = (double) s.get("y");
                Map<String, Object> e = (Map<String, Object>) line.get(line.size() - 1);
                double ex = (double) e.get("x");
                double ey = (double) e.get("y");

                RouteEntry routeEntry = getRouteFromWazeRouteId(id);
                if (routeEntry != null) {
                    DataEntry entry = new DataEntry(time, routeEntry, providerEntry);
                    entries.add(entry);
                }
            }
        } catch (Exception ex) {
            throw new RouteUnavailableException(providerName, "Data unreadable. Expected other structure." + json);
        }
        return entries;
    }
}
