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
    // weekdays: houdt voor elke dag een % bij. weekdays[0] = maandag, weekdays[7] = alle dagen
    public int[] weekdays;

    public IntervalTrafficData(int routeID, int speed, int time){
        this.routeID = routeID;
        this.speed = speed;
        this.time = time;
        weekdays = new int[8];

    }
    
    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(routeID).append("\": {");
        sb.append("\"speed\": \"").append(speed).append("\",");
        sb.append("\"time\": \"").append(time).append("\",");
        sb.append("\"days\": [");
        sb.append("\"").append(weekdays[0]).append("\"");
        for (int i = 1; i < 8; i++) {
          sb.append(",");
          sb.append("\"").append(weekdays[i]).append("\"");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
