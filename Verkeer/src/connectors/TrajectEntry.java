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
public class TrajectEntry {
    public String name;
    public double startCoordinateLatitude;
    public double startCoordinateLongitude;
    public double endCoordinateLatitude;
    public double endCoordinateLongitude;

    /**
     * 
     * @param name
     * @param startCoordinateLatitude
     * @param startCoordinateLongitude
     * @param endCoordinateLatitude
     * @param endCoordinateLongitude 
     */
    public TrajectEntry(String name, double startCoordinateLatitude, double startCoordinateLongitude, double endCoordinateLatitude, double endCoordinateLongitude) {
        this.name = name;
        this.startCoordinateLatitude = startCoordinateLatitude;
        this.startCoordinateLongitude = startCoordinateLongitude;
        this.endCoordinateLatitude = endCoordinateLatitude;
        this.endCoordinateLongitude = endCoordinateLongitude;
    }
    
    /**
     * 
     */
    public TrajectEntry(){}

    /**
     * Returns the name of the traject.
     * @return String with name of traject
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the latitude of the starting point.
     * @return double with latitude of starting point
     */
    public double getStartCoordinateLatitude() {
        return startCoordinateLatitude;
    }

    public void setStartCoordinateLatitude(double startCoordinateLatitude) {
        this.startCoordinateLatitude = startCoordinateLatitude;
    }

    /**
     * Returns the longitude of the starting point.
     * @return double with longitude of starting point
     */
    public double getStartCoordinateLongitude() {
        return startCoordinateLongitude;
    }
    
    public void setStartCoordinateLongitude(double startCoordinateLongitude) {
        this.startCoordinateLongitude = startCoordinateLongitude;
    }

    /**
     * Returns the latitude of the end point.
     * @return double with latitude of end point
     */
    public double getEndCoordinateLatitude() {
        return endCoordinateLatitude;
    }

    public void setEndCoordinateLatitude(double endCoordinateLatitude) {
        this.endCoordinateLatitude = endCoordinateLatitude;
    }

    /**
     * Returns the longitude of the end point.
     * @return double with longitude of end point
     */
    public double getEndCoordinateLongitude() {
        return endCoordinateLongitude;
    }

    public void setEndCoordinateLongitude(double endCoordinateLongitude) {
        this.endCoordinateLongitude = endCoordinateLongitude;
    }
    
}
