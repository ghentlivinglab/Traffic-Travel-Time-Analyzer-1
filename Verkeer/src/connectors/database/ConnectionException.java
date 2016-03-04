/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

/**
 *
 * @author Robin
 */
public class ConnectionException extends Exception{

    public ConnectionException() {
        super("Couldn't connect to database.");
    }
}
