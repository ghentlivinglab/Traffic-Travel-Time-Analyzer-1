/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.TrajectEntry;
import connectors.database.IDbConnector;
import java.util.List;

/**
 *
 * @author jarno
 */
public class GoogleProviderConnector extends AProviderConnector{
    private final static String API_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private final static String API_KEY = "AIzaSyCD3ESlkpUJGJvRKrJguvBa25eFNIJrujo"; // Jarno-Key
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
