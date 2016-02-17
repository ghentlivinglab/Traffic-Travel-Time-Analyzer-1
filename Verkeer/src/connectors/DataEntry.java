/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors;

import java.sql.Date;

/**
 *
 * @author Simon
 */
public class DataEntry {
    private Date timestamp;
    private double travelTime;
    private RouteEntry route;
    private ProviderEntry provider;

    /**
     * 
     * @param timestamp time of the measurement
     * @param travelTime estimated duration of the route
     * @param route route of the measurement
     * @param provider provider of the measurement
     */
    public DataEntry(Date timestamp, double travelTime, RouteEntry route, ProviderEntry provider) {
        this.timestamp = timestamp;
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
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the duration of the trip of this measurement.
     * @return integer with time in seconds
     */
    public double getTravelTime() {
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
}
