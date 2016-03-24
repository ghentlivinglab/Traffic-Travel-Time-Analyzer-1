
package verkeer;

import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Simon
 */
public class Verkeer {
    
    private static final Logger log = Logger.getLogger(Verkeer.class);
    
    public static void main(String[] args) throws IOException {
        log.info("Launching application");
        
        PollThread t = new PollThread();
        t.start();
        
        
        ConsoleParser cp = new ConsoleParser(t);
        cp.processCommandLineInput();
        
    }
}
