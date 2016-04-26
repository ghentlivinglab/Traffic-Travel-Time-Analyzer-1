package verkeer;

import connectors.provider.AProviderConnector;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

public class PollTask implements Runnable {

    private Collection<AProviderConnector> providers;
    private int updateCounter = 0;

    private static final Logger log = Logger.getLogger(PollControl.class);

    public void setProviders(Collection<AProviderConnector> providers) {
        this.providers = providers;
    }

    public int getUpdateCounter() {
        return updateCounter;
    }

    @Override
    public void run() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        log.info("nieuwe poll");

        // Enkel na 6 uur of voor half 1
        if (hours >= 6 || (hours == 0 && minutes <= 30)) {
            poll();
        } else {
            log.info("Closing for tonight. Goodnight xoxo");
            System.exit(0);
        }
        
        crunch();
        
        System.gc();
    }

    public void poll() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
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
    }
    
    public void crunch(){
        try {
            Runtime.getRuntime().exec("mysql -u test -ptest < crunch.mysql");
        } catch (IOException ex) {
            log.error("could not crunch the database data");
        }
    }
    
}
