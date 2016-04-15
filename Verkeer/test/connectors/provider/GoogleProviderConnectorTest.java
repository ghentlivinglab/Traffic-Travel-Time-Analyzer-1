package connectors.provider;

import connectors.RouteEntry;
import connectors.database.ConnectionException;
import connectors.database.DummyDbConnector;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GoogleProviderConnectorTest {

    static List<RouteEntry> trajecten;

    public GoogleProviderConnectorTest() {
    }

    /**
     * add a route to check the information from
     */
    @BeforeClass
    public static void setUpClass() {
        RouteEntry traject = new RouteEntry();
        traject.setName("R40 Drongensesteenweg -> Palinghuizen");
        traject.setStartCoordinateLatitude(51.0560905);
        traject.setStartCoordinateLongitude(3.6951634);
        traject.setEndCoordinateLatitude(51.0663037);
        traject.setEndCoordinateLongitude(3.6996797);
        trajecten = new ArrayList<>();
        trajecten.add(traject);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test to see if URL-creation is ok
     */
    @Test
    public void triggerTest(){
        DummyDbConnector dummy = new DummyDbConnector();
        int voor = dummy.getDataEntriesSize();
        int loops = 1;
        GoogleProviderConnector connector = new GoogleProviderConnector(dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+(dummy.getDataEntriesSize()-voor)+" given.");
        }
    }
}
