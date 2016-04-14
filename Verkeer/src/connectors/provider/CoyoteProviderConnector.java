/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.ning.http.client.AsyncHttpClient;
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
    private final Map<String, Integer> mapping;

    /**
     * Function that is called in the constructor to generate the mapping
     * between the Coyote ID and the database-ID
     */
    private void fillMapping() {
        mapping.put("Gasmeterlaan (R40) Eastbound - Palinghuizen - Neuseplein", 1);
        mapping.put("Paryslaan (R4) Northbound - Industrieweg - E34", 2);
        mapping.put("Paryslaan (R4) Southbound - E34 - Industrieweg", 3);
        mapping.put("Eisenhowerlaan (R4) Southbound - Kennedylaan - E17", 4);
        mapping.put("Heernislaan (R40) Northbound - Zuidparklaan - Dampoort", 5);
        mapping.put("Keizer Karelstraat Northbound - Sint-Lievenslaan - Neuseplein", 6);
        mapping.put("Brusselsesteenweg (N9) Eastbound - Scheldekaai - R4", 7);
        mapping.put("Brusselsesteenweg (N9) Westbound - R4 - Scheldekaai", 8);
        mapping.put("Drongensesteenweg (N466) Eastbound - E40 - Rooigemlaan", 9);
        mapping.put("B401 (Northbound) - E17 - Vlaanderenstraat", 10);
        mapping.put("Gasmeterlaan (R40) Westbound - Neuseplein - Palinghuizen", 11);
        mapping.put("Kennedylaan (R4) Southbound - E34 - Port Arthurlaan", 12);
        mapping.put("Martelaarslaan (R40) Northbound - Zuidparklaan - Drongensesteenweg", 13);
        mapping.put("Blaisantvest (N430) Westbound - Neuseplein - Einde Were", 14);
        mapping.put("Keizer Karelstraat Southbound - Neuseplein - Sint-Lievenslaan", 15);
        mapping.put("Binnenring-Drongen (R4) Northbound - Sluisweg - Industrieweg", 19);
        mapping.put("Heernislaan (R40) Southbound - Dampoort - Zuidparklaan", 16);
        mapping.put("Antwerpsesteenweg (N70) Eastbound - Dampoort - R4", 17);
        mapping.put("Rooigemlaan (R40) Southbound - Palinghuizen - Drongensesteenweg", 18);
        mapping.put("Kennedylaan (R4) Northbound - Port Arthurlaan - E34", 20);
        mapping.put("B401 (Southbound) - Vlaanderenstraat - E17", 21);
        mapping.put("Antwerpsesteenweg (N70) Westbound - R4 - Dampoort", 22);
        mapping.put("Dok-Noord (R40) Southbound - Neuseplein - Dampoort", 23);
        mapping.put("Martelaarslaan (R40) Southbound - Drongensesteenweg - Zuidparklaan", 24);
        mapping.put("Drongensesteenweg (N466) Westbound - Rooigemlaan - E40", 25);
        mapping.put("Dok-Noord (R40) Northbound - Dampoort - Neuseplein", 26);
        mapping.put("Rooigemlaan (R40) Northbound - Drongensesteenweg - Palinghuizen", 27);
        mapping.put("Buitenring-Drongen (R4) Southbound - Industrieweg - Sluisweg", 28);
        mapping.put("Blaisantvest (N430) Eastbound - Einde Were - Neuseplein", 29);
        mapping.put("Eisenhowerlaan (R4) Northbound - E17 - Kennedylaan", 30);
        mapping.put("Brugsevaart (N9) Northbound - Gebroeders de Smetstraat - R4", 31);
        mapping.put("Brugsevaart (N9) Southbound - R4 - Gebroeders de Smetstraat", 32);
        mapping.put("Oudenaardsesteenweg (N60) Northbound - E17 - R40", 34);
        mapping.put("Oudenaardsesteenweg (N60) Southbound - R40 - E17", 35);
    }

    /**
     * Constructs a new CoyoteProviderConnector with an IDbConnector to write
     * data to storage
     *
     * @param dbConnector connector to write DataEntry to
     */
    public CoyoteProviderConnector(IDbConnector dbConnector) {
        super(dbConnector, "Coyote");
        this.providerEntry = dbConnector.findProviderEntryByName(providerName);
        this.updateInterval = Integer.parseInt(prop.getProperty("COYOTE_UPDATE_INTERVAL"));
        this.dataFile = prop.getProperty("COYOTE_DATA_FILE");
        this.mapping = new HashMap<>();
        fillMapping();
    }

    @Override
    public void triggerUpdate(AsyncHttpClient a) {
        if (updateCounter % updateInterval == 0) {
            try {
                runPerl();
                readFile();
            } catch (FileNotFoundException e) {
                log.error("Perl script has not generated any output.");
            } catch (IOException | InterruptedException e) {
                System.out.println("fail");
                updateCounter++;
            }
        }
    }

    /**
     * Runs the perl-script named fetcher.pl which is in the ../Coyote_Perl folder
     * @throws IOException If an I/O error occurs when running the Perl-script
     * @throws InterruptedException if the current thread is interrupted by another thread while it is waiting for the Perl-script to finish, then the wait is ended and an InterruptedException is thrown.
     */
    protected void runPerl() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String[] perlCode = {"perl", prop.getProperty("COYOTE_SCRIPT"), providerEntry.getId() + "", providerEntry.getName(), dataFile};
        Process process = runtime.exec("perl "+prop.getProperty("COYOTE_SCRIPT")+" "+ providerEntry.getId()+ " " + providerEntry.getName()+" "+ dataFile);
        process.waitFor();
    }
    
    protected void readFile() throws FileNotFoundException{
        
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

                            data.setTimestamp(new Timestamp(Long.parseLong(entry.get("timestamp")) * 1000));
                            int travelTime = Math.round(Float.parseFloat(entry.get("real_time")));
                            data.setTravelTime(travelTime);

                            String name = entry.get("route_name");
                            int id;
                            try {
                                id = mapping.get(name);
                            } catch (NullPointerException e) {
                                log.error("Route \"" + name + "\" is not in the database.");
                                id = -1;
                            }
                            RouteEntry route = dbConnector.findRouteEntryByID(id);

                            data.setRoute(route);
                            data.setProvider(providerEntry);

                            dbConnector.insert(data);
                            buffer.nextLine();
                            break;
                        default:
                            entry.put(next, buffer.next());
                    }
                }
                buffer.close();
                if (file.delete()) {
                    //log.info(dataFile + " deleted");
                } else {
                    //log.info(dataFile + " not deleted");
                }

    }

    protected String getDataFile() {
        return dataFile;
    }
    
}
