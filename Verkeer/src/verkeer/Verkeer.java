
package verkeer;

import connectors.RouteEntry;
import connectors.database.MariaDbConnector;

/**
 *
 * @author Simon
 */
public class Verkeer {
    public static void main(String[] args) {
        
        RouteEntry entry = new RouteEntry();
        entry.setName("Test traject");
        entry.setLenght(123);
        entry.setIdealTravelTime(555);
        entry.setStartCoordinateLatitude(12);
        entry.setStartCoordinateLongitude(34);
        entry.setEndCoordinateLatitude(56);
        entry.setEndCoordinateLongitude(78);
        
        MariaDbConnector m = new MariaDbConnector();
        m.insert(entry);
        
    }
    
}
