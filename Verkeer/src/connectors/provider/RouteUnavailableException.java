package connectors.provider;

import org.apache.log4j.Logger;


public class RouteUnavailableException extends Exception {

    private static final Logger log = Logger.getLogger(RouteUnavailableException.class);
    
    public RouteUnavailableException() {
    }

    public RouteUnavailableException(String message) {
        super(message);
        log.error(message);
    }
}
