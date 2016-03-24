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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author HP
 */
public class PollThread extends Thread {
    private Properties prop;
    private Collection<AProviderConnector> providers;
    private IDbConnector dbcon;
    private int updateCounter = 0;
    
    private static final Logger log = Logger.getLogger(PollThread.class);
    
    public PollThread() {
        setDaemon(true);
        try {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("verkeer/app.properties");
            prop.load(is);
        } catch (IOException ex) {
            log.error("IOException: verkeer/app.properties could not be loaded.");
        }
        providers = new ArrayList<>();
        try{
            dbcon = new MariaDbConnector();
            log.info("Number of providers to instantiate: "+prop.getProperty("providerCount"));
            for(int i=0; i<Integer.parseInt(prop.getProperty("providerCount")); i++){
                try {
                    Class clazz = Class.forName(prop.getProperty("provider"+i));
                    Constructor ctor = clazz.getConstructor(IDbConnector.class);
                    AProviderConnector a = (AProviderConnector) ctor.newInstance(dbcon);
                    providers.add(a);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.error(ex.getMessage());
                }
                log.info(prop.getProperty("provider"+i) + "\t\t[LOADED] !");
            }
        }catch(ConnectionException e){
            log.info(e.getMessage());
        }
    }
    
    @Override
    public void run(){
        while(true){
            //System.out.println("\nTriggering update "+updateCounter);

            // Huidig uur inlezen
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);  
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);

            // Enkel na 6 uur of voor half 1
            if (hours >= 6 || (hours == 0 && minutes <= 30)){
                for(AProviderConnector a : providers){
                    a.triggerUpdate();
                }
                updateCounter++;
            }
            
                        
            try {
                Thread.sleep(Long.parseLong(prop.getProperty("pollinterval")));
            } catch (InterruptedException ex) {
                log.fatal("InterruptedException: pollthread is vastgelopen.");
            }
        }
    }

    public int getUpdateCounter() {
        return updateCounter;
    }
}
