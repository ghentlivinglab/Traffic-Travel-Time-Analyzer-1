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
    double avgTraveltime;
    
    public SimpleTrafficdata(String timestamp, double traveltime, double avgTraveltime) {
        this.timestamp = timestamp;
        this.traveltime = traveltime;
        this.avgTraveltime = avgTraveltime;
    }
    
    public SimpleTrafficdata(){}

    public String toJson(){
        StringBuilder s = new StringBuilder();
        
        s.append('"');
        s.append(timestamp);
        s.append("\": { \"traveltime\": ");
        s.append(traveltime);
        s.append(", \"average\": ");
        s.append(avgTraveltime);
        s.append("}");
        return s.toString();
    }
    
}
