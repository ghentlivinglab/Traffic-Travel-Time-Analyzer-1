package connectors;

public class RouteEntry {

    private int id;
    private String name;
    private String description;
    private double startCoordinateLatitude;
    private double startCoordinateLongitude;
    private double endCoordinateLatitude;
    private double endCoordinateLongitude;
    private int length;
    private int speedLimit;

    /**
     * @param id
     * @param name
     * @param description
     * @param startCoordinateLatitude
     * @param startCoordinateLongitude
     * @param endCoordinateLatitude
     * @param endCoordinateLongitude
     * @param length
     * @param speedLimit
     */
    public RouteEntry(int id, String name, String description, double startCoordinateLatitude, double startCoordinateLongitude, double endCoordinateLatitude, double endCoordinateLongitude, int length, int speedLimit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startCoordinateLatitude = startCoordinateLatitude;
        this.startCoordinateLongitude = startCoordinateLongitude;
        this.endCoordinateLatitude = endCoordinateLatitude;
        this.endCoordinateLongitude = endCoordinateLongitude;
        this.length = length;
        this.speedLimit = speedLimit;
    }

    /**
     *
     */
    public RouteEntry() {
    }

    /**
     * Returns the name of the traject.
     *
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
     *
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
     *
     * @return double with longitude of starting point
     */
    public double getStartCoordinateLongitude() {
        return startCoordinateLongitude;
    }

    public void setStartCoordinateLongitude(double startCoordinateLongitude) {
        this.startCoordinateLongitude = startCoordinateLongitude;
    }

    /**
     * Gets and sets the latitude of the end point.
     *
     * @return double with latitude of end point
     */
    public double getEndCoordinateLatitude() {
        return endCoordinateLatitude;
    }

    public void setEndCoordinateLatitude(double endCoordinateLatitude) {
        this.endCoordinateLatitude = endCoordinateLatitude;
    }

    /**
     * Gets and sets the longitude of the end point.
     *
     * @return double with longitude of end point
     */
    public double getEndCoordinateLongitude() {
        return endCoordinateLongitude;
    }

    public void setEndCoordinateLongitude(double endCoordinateLongitude) {
        this.endCoordinateLongitude = endCoordinateLongitude;
    }

    /**
     * Gets and sets the length of the route.
     *
     * @return integer with length of the route in meters
     */
    public int getLength() {
        return length;
    }

    public void setLenght(int lenght) {
        this.length = lenght;
    }

    /**
     * Gets and sets the ideal travel time of the route for the provider.
     *
     * @return integer with ideal travel time in seconds
     */
    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setIdealTravelTime(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    /**
     * Gets and sets the description of a RouteEntry object. This can be more specific then the name.
     * 
     * @return String with description
     */
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets and sets the id of the route
     *
     * @return integer with id of the route
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object other){
        if(other == null) return false;
        if(other == this) return true;
        if(!(other instanceof RouteEntry)) return false;
        RouteEntry otherRouteEntry = (RouteEntry) other;
        if( otherRouteEntry.getName().equals(name) &&
                otherRouteEntry.getStartCoordinateLatitude() == startCoordinateLatitude &&
                otherRouteEntry.getStartCoordinateLongitude() == startCoordinateLongitude &&
                otherRouteEntry.getEndCoordinateLatitude() == endCoordinateLatitude &&
                otherRouteEntry.getEndCoordinateLongitude() == endCoordinateLongitude &&
                otherRouteEntry.getDescription().equals(description) &&
                otherRouteEntry.getLength() == length &&
                otherRouteEntry.getSpeedLimit() == speedLimit) return true;
        else return false;
    }
}
