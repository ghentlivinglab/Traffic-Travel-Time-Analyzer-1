/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import connectors.DataEntry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {

    protected Properties prop;
    protected IDbConnector dbConnector;
    protected Collection<RouteEntry> routes;
    protected ProviderEntry providerEntry;

    /**
     * Constructs a new abstract ProviderConnector with an IDbConnector to write
     * data to storage
     *
     * @param dbConnector connector to write DataEntry to
     */
    public AProviderConnector(IDbConnector dbConnector) {
        this.dbConnector = dbConnector;
        this.routes = dbConnector.findAllRouteEntries();
        
        try{
            prop = new Properties();
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("connectors/provider/providers.properties");
            if(propsFile == null){
                System.err.println("connectors/provider/providers.properties kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e){
            System.err.println("connectors/provider/providers.properties niet gevonden.");
        }catch( IOException ee){
            System.err.println("connectors/provider/providers.properties kon niet geladen worden.");
        }
    }

    /**
     * Makes the ProviderConnector fetch data from provider
     */
    public abstract void triggerUpdate();

    /**
     * Saves the generated info to the IDbConnector
     *
     * @param entry
     */
    public void saveToDb(DataEntry entry) {
        dbConnector.insert(entry);
    }
}
