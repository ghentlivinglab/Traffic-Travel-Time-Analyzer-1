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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

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
            FileInputStream fis = new FileInputStream(new File("./config/app.properties"));
            prop.load(fis);
        } catch (IOException ex) {
            System.err.println("./config/app.properties niet gevonden. " + ex.getMessage());
            System.exit(1);

        }
        providers = new ArrayList<>();
        try {
            dbcon = new MariaDbConnector();
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
        } catch (ConnectionException e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        while (true) {
            // Huidig uur inlezen
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            
            log.info("nieuwe poll");

            // Enkel na 6 uur of voor half 1
            if (hours >= 6 || (hours == 0 && minutes <= 30)) {
                for (AProviderConnector a : providers) {
                    if (a.shouldTriggerUpdate()) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                a.triggerUpdate();
                            }
                        });
                    }
                    a.increaseUpdateCounter();
                }
                updateCounter++;
            } else {
                log.info("No update because its between 00:30am and 6:00am");
            }
            try {
                Thread.sleep(Long.parseLong(prop.getProperty("pollinterval")));
            } catch (InterruptedException ex) {
                log.fatal("InterruptedException: pollthread is vastgelopen.");
                System.exit(1);
            }
        }
    }

    public void reloadProperties() {
        for (AProviderConnector apc : providers) {
            apc.reloadProperties();
        }
        try {
            dbcon.reloadProperties();
        } catch (ConnectionException e) {
            log.error("Reload failed. Could not make a connection to the database.");
            System.exit(1);
        }
    }

    public int getUpdateCounter() {
        return updateCounter;
    }

    public void forceUpdate() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        for (AProviderConnector a : providers) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    a.triggerUpdate();
                }
            });
        }
        updateCounter++;
    }
}
