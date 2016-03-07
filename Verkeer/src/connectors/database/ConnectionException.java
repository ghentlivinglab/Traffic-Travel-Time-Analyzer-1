/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import java.io.IOException;
import java.util.logging.Level;
import verkeer.MyLogger;
import verkeer.Verkeer;

/**
 *
 * @author Robin
 */
public class ConnectionException extends Exception implements MyLogger{

    public ConnectionException() {
        super("Couldn't connect to database.");
        doLog(Level.WARNING, "Kon niet verbinden met de database.");
    }

    @Override
    public void doLog(Level lvl, String log) {
        try{
            Verkeer.getLogger(ConnectionException.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }
}
