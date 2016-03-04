/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package verkeer;

import connectors.database.IDbConnector;
import connectors.database.MariaDbConnector;
import connectors.provider.AProviderConnector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP
 */
public class PollThread extends Thread implements MyLogger{
    Properties prop;

    Collection<AProviderConnector> providers;
    IDbConnector dbcon;
    
    int updateCounter = 0;
    public PollThread() {
        setDaemon(true);
        try {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("verkeer/app.properties");
            prop.load(is);
        } catch (IOException ex) {
            doLog(Level.WARNING, "app.properties kon niet geladen worden.");
        }
        providers = new ArrayList<>();
        dbcon = new MariaDbConnector();
        doLog(Level.INFO, "Number of providers to instantiate: "+prop.getProperty("providerCount"));
        for(int i=0; i<Integer.parseInt(prop.getProperty("providerCount")); i++){
            doLog(Level.INFO, "  - "+prop.getProperty("provider"+i));
            try {
                Class clazz = Class.forName(prop.getProperty("provider"+i));
                Constructor ctor = clazz.getConstructor(IDbConnector.class);
                AProviderConnector a = (AProviderConnector) ctor.newInstance(dbcon);
                providers.add(a);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                doLog(Level.WARNING, ex.getMessage());
                System.err.println(ex.getMessage());
            }
            doLog(Level.INFO, "  - "+prop.getProperty("provider"+i) + "\t\t[LOADED] !");
        }
        System.out.println("");
    }
    
    @Override
    public void run(){
        while(true){
            try {
                Thread.sleep(Long.parseLong(prop.getProperty("pollinterval")));    //10s
            } catch (InterruptedException ex) {
                doLog(Level.SEVERE, ex.getMessage());
            }
            for(AProviderConnector a : providers){
                a.triggerUpdate();
            }
            updateCounter++;
        }
    }
     
    @Override
    public void doLog(Level lvl, String log) {
        try{
            Verkeer.getLogger(PollThread.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }

}
