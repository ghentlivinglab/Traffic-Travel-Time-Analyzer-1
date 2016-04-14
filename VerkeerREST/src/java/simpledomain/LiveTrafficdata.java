/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledomain;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Robin
 */
public class LiveTrafficdata {
    int routeID;
    public Map<String,String> live;
    public Map<String,String> avg;
    public LiveTrafficdata(int routeID){
        this.routeID = routeID;
        live = new HashMap<>();
        avg = new HashMap<>();
    }
    
    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(routeID).append("\": {");
        sb.append("\"live\": {");
        sb.append("\"createdOn\": \"").append(live.get("createdOn")).append("\",");
        sb.append("\"speed\": \"").append(live.get("speed")).append("\",");
        sb.append("\"time\": \"").append(live.get("time")).append("\"");
        sb.append("}, \"avg\": {");
        sb.append("\"speed\": \"").append(avg.get("speed")).append("\",");
        sb.append("\"time\": \"").append(avg.get("time")).append("\"");
        sb.append("}");
        sb.append('}');
        return sb.toString();
    }
}
