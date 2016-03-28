/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledomain;

import java.io.Serializable;

/**
 *
 * @author Piet
 */
public class SimpleWaypoint implements Serializable{

    int routeID;
    int sequence;
    double latitude;
    double longitude;

    public SimpleWaypoint(int routeID, int sequence, double latitude, double longitude) {
        this.routeID = routeID;
        this.sequence = sequence;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
