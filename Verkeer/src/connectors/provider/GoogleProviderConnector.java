/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.RouteEntry;
import connectors.database.IDbConnector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

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
        this.providerEntry = dbConnector.findByName(providerName);
    }

    @Override
    public void triggerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet.");
        for (RouteEntry traject : trajecten) {
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

}
