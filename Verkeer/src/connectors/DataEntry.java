/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Simon
 */
public class DataEntry {
    private Timestamp timestamp;
    private int travelTime;
    private RouteEntry route;
    private ProviderEntry provider;

    /**
     * 
     * @param timestamp time of the measurement
     * @param travelTime estimated duration of the route
     * @param route route of the measurement
     * @param provider provider of the measurement
     */
    public DataEntry(Timestamp timestamp, int travelTime, RouteEntry route, ProviderEntry provider) {
        this.timestamp = timestamp;
        this.travelTime = travelTime;
        this.route = route;
        this.provider = provider;
    }
    /**
     * Convenient constructor. Uses 'now' as timestamp
     * @param travelTime estimated duration of the route
     * @param route route of the measurement
     * @param provider provider of the measurement
     */
    public DataEntry(int travelTime, RouteEntry route, ProviderEntry provider) {
        this.setTimestampNow();
        this.travelTime = travelTime;
        this.route = route;
        this.provider = provider;
    }
    
    /**
     * 
     */
    public DataEntry(){
        this.route = new RouteEntry();
        this.provider = new ProviderEntry();
    }

    /**
     * Returns the time of the measurement.
     * @return integer with time in seconds
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    
    public final void setTimestampNow(){ 
        // create a java calendar instance
        Calendar calendar = Calendar.getInstance();

        // get a java date (java.util.Date) from the Calendar instance.
        // this java date will represent the current date, or "now".
        java.util.Date currentDate = calendar.getTime();

        // now, create a java.sql.Date from the java.util.Date
        this.timestamp = new Timestamp(currentDate.getTime());
    }

    /**
     * Returns the duration of the trip of this measurement.
     * @return integer with time in seconds
     */
    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    /**
     * Returns the RouteEntry-object of this measurement.
     * @return  RouteEntry with route-info
     */
    public RouteEntry getRoute() {
        return route;
    }

    public void setRoute(RouteEntry route) {
        this.route = route;
    }

    /**
     * Returns the ProviderEntry-object of this measurement.
     * @return ProviderEntry with provider-info
     */
    public ProviderEntry getProvider() {
        return provider;
    }

    public void setProvider(ProviderEntry provider) {
        this.provider = provider;
    }
    
    @Override
    public String toString(){
        // Convert Timestamp in readable string
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String reportDate = df.format(timestamp);

        return "DataEntry {\n\troute: \""+route+"\",\n\ttimestamp: \""+reportDate+"\",\n\ttravelTime: "+travelTime+",\n\tprovider: \""+provider.getName()+"\"\n}";
    }
}
