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
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP
 */
public class PollThread extends Thread{
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
            //System.err.println("app.properties kon niet geladen worden.");
        }
        providers = new ArrayList<>();
        try{
            dbcon = new MariaDbConnector();
            System.out.println("Number of providers to instantiate: "+prop.getProperty("providerCount"));
            for(int i=0; i<Integer.parseInt(prop.getProperty("providerCount")); i++){
                System.out.print("  - "+prop.getProperty("provider"+i));
                try {
                    Class clazz = Class.forName(prop.getProperty("provider"+i));
                    Constructor ctor = clazz.getConstructor(IDbConnector.class);
                    AProviderConnector a = (AProviderConnector) ctor.newInstance(dbcon);
                    providers.add(a);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(PollThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("\t\t[LOADED] !");
            }
            System.out.println("");
        }catch(ConnectionException e){
            System.out.println("\n"+e.getMessage());
        }
    }
    
    @Override
    public void run(){
        while(true){
            try {
                //Thread.sleep(300000);   //5min
                Thread.sleep(Long.parseLong(prop.getProperty("pollinterval")));    //10s
            } catch (InterruptedException ex) {
                Logger.getLogger(PollThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("\nTriggering update "+updateCounter);
            for(AProviderConnector a : providers){
                a.triggerUpdate();
                //System.out.println("Provider klaar");
            }
            updateCounter++;
        }
    }
    
    
}
