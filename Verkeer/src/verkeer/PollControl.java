/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import connectors.database.ConnectionException;
import connectors.database.IDbConnector;
import connectors.database.MariaDbConnector;
import connectors.provider.AProviderConnector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Robin
 */
public class PollControl {
    private PollTask pt;
    private ScheduledExecutorService ses;
    private Properties prop;
    private Collection<AProviderConnector> providers;
    private IDbConnector dbcon;
    private int updateCounter = 0;
    
    private static final Logger log = Logger.getLogger(PollControl.class);
    
    public void init(){
        try{
            pt = new PollTask();
            loadProperties();
            loadDbConnector();
            loadProviders();
            
            pt.setProviders(providers);
            
            startPolling();
            
        }catch( ConnectionException e ){
            log.info(e.getMessage());
        }
    }
    
    private void loadProperties(){
        try {
            prop = new Properties();
            FileInputStream fis = new FileInputStream(new File("./config/app.properties"));
            prop.load(fis);
            fis.close();
        } catch (IOException ex) {
            System.err.println("./config/app.properties niet gevonden. " + ex.getMessage());
            System.exit(1);
        }
    }
    
    private void loadDbConnector() throws ConnectionException{
        dbcon = new MariaDbConnector();
    }
    
    private void loadProviders(){
        providers = new ArrayList<>();
        log.info("Number of providers to instantiate: " + prop.getProperty("providerCount"));
        for (int i = 0; i < Integer.parseInt(prop.getProperty("providerCount")); i++) {
            try {
                Class clazz = Class.forName(prop.getProperty("provider" + i));
                Constructor ctor = clazz.getConstructor(IDbConnector.class);
                AProviderConnector a = (AProviderConnector) ctor.newInstance(dbcon);
                providers.add(a);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.error(ex.getMessage());
            }
            log.info(prop.getProperty("provider" + i) + "\t\t[LOADED] !");
        }
    }
    
    public void startPolling(){
        ses = new ScheduledThreadPoolExecutor(2);
        ses.scheduleWithFixedDelay(pt, 0, 5, TimeUnit.MINUTES);
    }
    
    public void stopPolling(){
        ses.shutdown();
    }
    
    public void forcePoll(){
        pt.poll();
    }
    
    public int getUpdateCounter(){
        return pt.getUpdateCounter();
    }
    
}
