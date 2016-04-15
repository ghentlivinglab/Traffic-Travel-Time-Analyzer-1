package connectors.provider;

import org.apache.log4j.Logger;

public class NotAuthorizedException extends Exception {

    private static final Logger log = Logger.getLogger(NotAuthorizedException.class);
    
    public NotAuthorizedException() {
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
