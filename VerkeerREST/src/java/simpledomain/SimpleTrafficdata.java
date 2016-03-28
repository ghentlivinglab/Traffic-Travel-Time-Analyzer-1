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

    Timestamp timestamp;
    double traveltime;

    public SimpleTrafficdata(Timestamp timestamp, double traveltime) {
        this.timestamp = timestamp;
        this.traveltime = traveltime;
    }

}
