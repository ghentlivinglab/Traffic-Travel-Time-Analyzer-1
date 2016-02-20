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
public class RouteEntry {
    private int id;
    private String name;
    private double startCoordinateLatitude;
    private double startCoordinateLongitude;
    private double endCoordinateLatitude;
    private double endCoordinateLongitude;
    private int lenght;
    private int idealTravelTime;

    /**
     * 
     * @param name
     * @param startCoordinateLatitude
     * @param startCoordinateLongitude
     * @param endCoordinateLatitude
     * @param endCoordinateLongitude
     * @param lenght
     * @param idealTravelTime 
     */
    public RouteEntry(String name, double startCoordinateLatitude, double startCoordinateLongitude, double endCoordinateLatitude, double endCoordinateLongitude, int lenght, int idealTravelTime) {
        this.name = name;
        this.startCoordinateLatitude = startCoordinateLatitude;
        this.startCoordinateLongitude = startCoordinateLongitude;
        this.endCoordinateLatitude = endCoordinateLatitude;
        this.endCoordinateLongitude = endCoordinateLongitude;
        this.lenght = lenght;
        this.idealTravelTime = idealTravelTime;
    }
    
    /**
     * 
     */
    public RouteEntry(){}

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
    
    /**
     * Returns the lenght of the route.
     * @return integer with lenght of the route in meters
     */
    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    /**
     * Returns the ideal travel time of the route for the provider.
     * @return integer with ideal travel time in seconds
     */
    public int getIdealTravelTime() {
        return idealTravelTime;
    }

    public void setIdealTravelTime(int idealTravelTime) {
        this.idealTravelTime = idealTravelTime;
    }

    /**
     * Returns the id of the route
     * @return integer with id of the route
     */
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    
}
