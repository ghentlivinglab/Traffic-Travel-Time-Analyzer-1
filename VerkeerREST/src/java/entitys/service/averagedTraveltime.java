/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys.service;

import java.sql.Timestamp;

/**
 *
 * @author Piet
 */
public class averagedTraveltime {
    public Timestamp avgTimestamp;
    public int avgTraveltime;
 
    public averagedTraveltime(Timestamp avgTimestamp, int avgTraveltime) {
        this.avgTimestamp = avgTimestamp;
        this.avgTraveltime = avgTraveltime;
    }

    public Timestamp getAvgTimestamp() {
        return avgTimestamp;
    }

    public void setAvgTimestamp(Timestamp avgTimestamp) {
        this.avgTimestamp = avgTimestamp;
    }

    public int getAvgTraveltime() {
        return avgTraveltime;
    }

    public void setAvgTraveltime(int avgTraveltime) {
        this.avgTraveltime = avgTraveltime;
    }
    
    
}

