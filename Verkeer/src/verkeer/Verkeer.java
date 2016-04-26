package verkeer;

import org.apache.log4j.Logger;

public class Verkeer {

    private static final Logger log = Logger.getLogger(Verkeer.class);

    public static void main(String[] args) {
        try {
            log.info("Launching application");

            PollControl pollControl = new PollControl();
            pollControl.init();
            
            ConsoleParser cp = new ConsoleParser(pollControl);
            cp.processCommandLineInput();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
