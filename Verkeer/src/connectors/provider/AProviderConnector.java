/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.ning.http.client.AsyncHttpClient;
import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import connectors.DataEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import verkeer.PollThread;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {

    protected Properties prop;
    protected IDbConnector dbConnector;
    protected Collection<RouteEntry> routes;
    protected ProviderEntry providerEntry;
    protected int updateCounter=1;
    protected int updateInterval=0;  
    protected String providerName;
    /**
     * Constructs a new abstract ProviderConnector with an IDbConnector to write
     * data to storage
     *
     * @param dbConnector connector to write DataEntry to
     * @param providerName name of the provider
     */
    public AProviderConnector(IDbConnector dbConnector, String providerName) {
        this.dbConnector = dbConnector;
        this.routes = dbConnector.findAllRouteEntries();
        this.providerName = providerName;
        try{
            prop = new Properties();
            FileInputStream fis = new FileInputStream(new File(AProviderConnector.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent()+"/config/providers.properties");
            prop.load(fis);
        }catch( FileNotFoundException e){
            System.err.println("./config/providers.properties niet gevonden.");
        }catch( IOException ee){
            System.err.println("./config/providers.properties kon niet geladen worden.");
        }
    }

    /**
     * Makes the ProviderConnector fetch data from provider
     * @param a The AsyncHttpClient used to send the requests.
     */
    public abstract void triggerUpdate(AsyncHttpClient a);

    /**
     * Saves the generated info to the IDbConnector
     *
     * @param entry
     */
    public void saveToDb(DataEntry entry) {
        dbConnector.insert(entry);
    }
    
    public void reloadProperties(){
        this.routes = dbConnector.findAllRouteEntries();
        try{
            prop = new Properties();
            FileInputStream fis = new FileInputStream(new File(AProviderConnector.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent()+"/config/providers.properties");
            prop.load(fis);
        }catch( FileNotFoundException e){
            System.err.println("./config/providers.properties niet gevonden.");
        }catch( IOException ee){
            System.err.println("./config/providers.properties kon niet geladen worden.");
        }
    }
    
}
