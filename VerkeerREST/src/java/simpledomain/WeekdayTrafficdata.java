/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledomain;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author Piet
 */
public class WeekdayTrafficdata {

    int weekday;
    ArrayList<SimpleTrafficdata> data;

    public WeekdayTrafficdata(int weekday) {
        this.weekday = weekday;
        data = new ArrayList<>();
    }
    
    public void put(String timestamp, double traveltime){
        data.add(new SimpleTrafficdata(timestamp, traveltime));
    }
    
    public String toJson(){
        StringBuilder s = new StringBuilder();
        s.append('"').append(weekday).append('"');
        s.append(": {");
        String delimiter = "";
        for(SimpleTrafficdata std : data){
            s.append(delimiter).append(std.toJson());
            delimiter=",";
        }
        s.append('}');
        return s.toString();
    }
    
}
