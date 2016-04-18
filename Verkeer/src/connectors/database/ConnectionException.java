package connectors.database;

import org.apache.log4j.Logger;

public class ConnectionException extends Exception {

    private static final Logger log = Logger.getLogger(ConnectionException.class);

    public ConnectionException() {
        super("Couldn't connect to database.");
        log.error(getMessage());
    }
}
