/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Robin
 */
public class IntervalTrafficData {
    int routeID;
    int speed;
    int time;
    String unusual = null;
    int slowPercentage;

    public IntervalTrafficData(int routeID, int speed, int time, int slowPercentage, String unusual){
        this.routeID = routeID;
        this.speed = speed;
        this.time = time;
        this.slowPercentage = slowPercentage;
        this.unusual = unusual;

    }
    
    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(routeID).append("\": {");
        sb.append("\"speed\": \"").append(speed).append("\",");
        sb.append("\"time\": \"").append(time).append("\",");
        sb.append("\"slow\": \"").append(slowPercentage).append("\"");
        if (unusual != null) {
            sb.append(", \"unusual\": [").append(unusual).append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
