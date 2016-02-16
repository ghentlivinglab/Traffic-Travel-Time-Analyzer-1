/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.owlike.genson.Genson;
import com.owlike.genson.stream.ObjectReader;
import connectors.TrajectEntry;
import connectors.database.IDbConnector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Logger;

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

    public GoogleProviderConnector(List<TrajectEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        String providerName = "Google Maps";
        this.providerEntry = dbConnector.getProvider(providerName);
    }

    @Override
    public void triggerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet.");
        for (TrajectEntry traject : trajecten) {
            try {
                URL url = generateURL(traject);
                URLConnection connection = url.openConnection();
                connection.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();
                
                

            } catch (MalformedURLException e) { // exception when url invalid

            } catch (IOException e) { // exception when connection failed
            }

        }
    }

    protected URL generateURL(TrajectEntry traject) throws MalformedURLException {
        StringBuilder urlBuilder = new StringBuilder(API_URL);
        urlBuilder.append("?key=");
        urlBuilder.append(API_KEY);
        urlBuilder.append("&origin=");
        urlBuilder.append(traject.startCoordinateLatitude);
        urlBuilder.append(",");
        urlBuilder.append(traject.startCoordinateLongitude);
        urlBuilder.append("&destination=");
        urlBuilder.append(traject.endCoordinateLatitude);
        urlBuilder.append(",");
        urlBuilder.append(traject.endCoordinateLongitude);

        URL url = new URL(urlBuilder.toString());

        return url;

    }

}
