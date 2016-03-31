/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledomain;

import java.sql.Timestamp;

/**
 *
 * @author Piet
 */
public class SimpleTrafficdata {

    String timestamp;
    double traveltime;

    public SimpleTrafficdata(String timestamp, double traveltime) {
        this.timestamp = timestamp;
        this.traveltime = traveltime;
    }
    
    public SimpleTrafficdata(){}

    public String toJson(){
        StringBuilder s = new StringBuilder();
        
        s.append('"');
        s.append(timestamp);
        s.append("\": ");
        s.append(traveltime);
        return s.toString();
    }
    
}
