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
    public float startCoordinateLatitude;
    public float startCoordinateLongitude;
    public float endCoordinateLatitude;
    public float endCoordinateLongitude;

    /**
     * 
     * @param name
     * @param startCoordinateLatitude
     * @param startCoordinateLongitude
     * @param endCoordinateLatitude
     * @param endCoordinateLongitude 
     */
    public TrajectEntry(String name, float startCoordinateLatitude, float startCoordinateLongitude, float endCoordinateLatitude, float endCoordinateLongitude) {
        this.name = name;
        this.startCoordinateLatitude = startCoordinateLatitude;
        this.startCoordinateLongitude = startCoordinateLongitude;
        this.endCoordinateLatitude = endCoordinateLatitude;
        this.endCoordinateLongitude = endCoordinateLongitude;
    }

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
     * @return float with latitude of starting point
     */
    public float getStartCoordinateLatitude() {
        return startCoordinateLatitude;
    }

    public void setStartCoordinateLatitude(float startCoordinateLatitude) {
        this.startCoordinateLatitude = startCoordinateLatitude;
    }

    /**
     * Returns the longitude of the starting point.
     * @return float with longitude of starting point
     */
    public float getStartCoordinateLongitude() {
        return startCoordinateLongitude;
    }
    
    public void setStartCoordinateLongitude(float startCoordinateLongitude) {
        this.startCoordinateLongitude = startCoordinateLongitude;
    }

    /**
     * Returns the latitude of the end point.
     * @return float with latitude of end point
     */
    public float getEndCoordinateLatitude() {
        return endCoordinateLatitude;
    }

    public void setEndCoordinateLatitude(float endCoordinateLatitude) {
        this.endCoordinateLatitude = endCoordinateLatitude;
    }

    /**
     * Returns the longitude of the end point.
     * @return float with longitude of end point
     */
    public float getEndCoordinateLongitude() {
        return endCoordinateLongitude;
    }

    public void setEndCoordinateLongitude(float endCoordinateLongitude) {
        this.endCoordinateLongitude = endCoordinateLongitude;
    }
    
}
