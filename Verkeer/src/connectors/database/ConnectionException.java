/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import org.apache.log4j.Logger;

/**
 *
 * @author Robin
 */
public class ConnectionException extends Exception{

    private static final Logger log = Logger.getLogger(ConnectionException.class);
    
    public ConnectionException() {
        super("Couldn't connect to database.");
        log.error(getMessage());
    }
}
