package connectors.provider;

import connectors.RouteEntry;
import connectors.database.DummyDbConnector;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void URLCreationTest() {
        //String correctURL = GoogleProviderConnector.API_URL + "?key=" + GoogleProviderConnector.API_KEY + "&origin=51.0560905,3.6951634&destination=51.0663037,3.6996797";
        //GoogleProviderConnector connector = new GoogleProviderConnector(new DummyDbConnector());
        // TODO: 'trajecten' is slechts een tijdelijke placeholder.
        //assertEquals(connector.generateURL(trajecten.get(0)), correctURL);
    }

    /**
     * test if a request goes through
     */
    @Test
    public void connectionTest() {
        try {
            GoogleProviderConnector connector = new GoogleProviderConnector(new DummyDbConnector());
            connector.triggerUpdate();

        } catch (Exception e) { // connection can only fail if
            fail(e.getMessage());
        }
    }
}
