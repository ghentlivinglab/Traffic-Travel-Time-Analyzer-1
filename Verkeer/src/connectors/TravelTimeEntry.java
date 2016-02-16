/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors;

/**
 *
 * @author Simon
 */
public class TravelTimeEntry {
    private int timestamp;
    private int travelTime;
    private TrajectEntry traject;
    private ProviderEntry provider;

    /**
     * 
     * @param timestamp time of the measurement
     * @param travelTime estimated duration of the traject
     * @param traject traject of the measurement
     * @param provider provider of the measurement
     */
    public TravelTimeEntry(int timestamp, int travelTime, TrajectEntry traject, ProviderEntry provider) {
        this.timestamp = timestamp;
        this.travelTime = travelTime;
        this.traject = traject;
        this.provider = provider;
    }

    /**
     * Returns the time of the measurement.
     * @return integer with time in seconds
     */
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
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
     * Returns the TrajectEntry-object of this measurement.
     * @return  TrajectEntry with traject-info
     */
    public TrajectEntry getTraject() {
        return traject;
    }

    public void setTraject(TrajectEntry traject) {
        this.traject = traject;
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
