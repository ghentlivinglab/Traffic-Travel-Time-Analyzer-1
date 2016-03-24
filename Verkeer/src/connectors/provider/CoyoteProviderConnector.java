/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.DataEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 *
 * @author jarno
 */
public class CoyoteProviderConnector extends AProviderConnector {

    private final String dataFile;
    private static final Logger log = Logger.getLogger(CoyoteProviderConnector.class);

    public CoyoteProviderConnector(IDbConnector dbConnector) {
        super(dbConnector,"Coyote");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        updateInterval = Integer.parseInt(prop.getProperty("COYOTE_UPDATE_INTERVAL"));
        dataFile = prop.getProperty("COYOTE_DATA_FILE");
    }

    @Override
    public void triggerUpdate() {
        if (updateCounter % updateInterval == 0) {
            try {
                Runtime runtime = Runtime.getRuntime();
                String[] perlCode = {"perl", "../Coyote_Perl/fetcher.pl", providerEntry.getId() + "", providerEntry.getName(), dataFile};
                Process process = runtime.exec(perlCode);
                process.waitFor();
                File file = new File(dataFile);
                Scanner buffer = new Scanner(file);
                buffer.useDelimiter("\n|:");

                Map<String, String> entry = new HashMap<>();

                while (buffer.hasNext()) {
                    String next = buffer.next();

                    switch (next) {
                        case "ENTRY":
                            entry = new HashMap<>();
                            break;
                        case "END":
                            DataEntry data = new DataEntry();
                            RouteEntry route = new RouteEntry();

                            data.setProvider(providerEntry);
                            data.setRoute(route);
                            data.setTimestamp(new Timestamp(Long.parseLong(entry.get("timestamp")) * 1000));
                            int travelTime = Math.round(Float.parseFloat(entry.get("real_time")));
                            data.setTravelTime(travelTime);

                            route.setEndCoordinateLatitude(Double.parseDouble(entry.get("end_lat")));
                            route.setEndCoordinateLongitude(Double.parseDouble(entry.get("end_lon")));
                            route.setId(-1); // TODO match ID's of routes to db-id's
                            int idealTime = Math.round(Float.parseFloat(entry.get("normal_time")));
                            route.setIdealTravelTime(idealTime);
                            route.setLenght(Integer.parseInt(entry.get("length")));
                            route.setName(entry.get("route_name"));
                            route.setStartCoordinateLatitude(Double.parseDouble(entry.get("start_lat")));
                            route.setStartCoordinateLongitude(Double.parseDouble(entry.get("start_lat")));

                            dbConnector.insert(data);
                            buffer.nextLine();
                            break;
                        default:
                            entry.put(next, buffer.next());
                    }
                }
                buffer.close();
                if(file.delete()){
                    log.info(dataFile+" deleted");
                } else{
                    log.info(dataFile+" not deleted");
                }

            } catch (FileNotFoundException e) {
                // TODO perl has borked and not generated output
            } catch (IOException | InterruptedException e) {
                System.out.println("fail");
                updateCounter++;
            }
        }
    }

}
