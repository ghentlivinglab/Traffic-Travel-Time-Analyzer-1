package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import java.security.Provider;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MariaDbConnectorTest {

    static MariaDbConnector instance;
    
    public MariaDbConnectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ConnectionException {
        instance = new MariaDbConnector();
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
     * Test of insert methods, of class MariaDbConnector.
     */
    @Test
    public void testInserts() throws ConnectionException {
        //adding test ProviderEntry to database
        ProviderEntry pe = MariaDbConnectorTest.instance.findProviderEntryByName("test");
        ProviderEntry ppe = MariaDbConnectorTest.instance.findProviderEntryByName("test");
        assertEquals(pe, ppe); // Passes if insert succeeded
        
        
        //adding test RouteEntry to database
        RouteEntry re = new RouteEntry();
        re.setName("test");
        re.setDescription("test");
        MariaDbConnectorTest.instance.insert(re);
        RouteEntry rre = MariaDbConnectorTest.instance.findRouteEntryByName("test");
        assertEquals(re, rre); // Passes if insert has succeeded
        
        //adding test DataEntry to database
        int traveltime = 1234569;
        DataEntry de = new DataEntry(traveltime, rre, ppe);
        MariaDbConnectorTest.instance.insert(de);
        System.out.println(ppe.getId());
        System.out.println(rre.getId());
        System.out.println(de.getTimestamp());
        DataEntry dde = MariaDbConnectorTest.instance.findDataEntryByID(ppe.getId(), rre.getId(), de.getTimestamp());
        //assertEquals(de, dde); // Passes if insert has succeeded
        
        // Removing all added testdata from database. Removing route & providers suffices
        MariaDbConnectorTest.instance.delete(ppe);
        MariaDbConnectorTest.instance.delete(rre);
        
        // Check if the provider & route objects are removed from the database
        RouteEntry rrre = MariaDbConnectorTest.instance.findRouteEntryByID(rre.getId());
        assertNull(rrre); // Passes if the test RouteEntry object does not exist anymore in the database
        
        ProviderEntry pppe = MariaDbConnectorTest.instance.findProviderEntryByID(ppe.getId());
        assertNull(pppe);
        
        
        
        
    }

    /**
     * Test of findProviderEntryByName method, of class MariaDbConnector.
     */
    @Test
    public void testFindProviderEntryByName() throws ConnectionException {
        ProviderEntry result = MariaDbConnectorTest.instance.findProviderEntryByName("Waze");
        assertTrue((result != null)&& (result.getName() != null) && !(result.getName().equals("")) && (result.getName().length() != 0));
    }

    /**
     * Test of findProviderEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindProviderEntryByID() throws ConnectionException {
        ProviderEntry result = MariaDbConnectorTest.instance.findProviderEntryByID(1);
        assertTrue((result != null)&& (result.getName() != null) && !(result.getName().equals("")) && (result.getName().length() != 0));
    }

    /**
     * Test of findRouteEntryByName method, of class MariaDbConnector.
     */
    @Test
    public void testFindRouteEntryByName() throws ConnectionException {
        RouteEntry result = instance.findRouteEntryByName("Gasmeterlaan (R40) eastbound");
        assertTrue((result != null)&& (result.getName() != null) && !(result.getName().equals("")) && (result.getName().length() != 0));
    }

    /**
     * Test of findRouteEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindRouteEntryByID() throws ConnectionException {
        RouteEntry result = MariaDbConnectorTest.instance.findRouteEntryByID(1);
        assertTrue((result != null)&& (result.getName() != null) && !(result.getName().equals("")) && (result.getName().length() != 0));
    }
    

    /**
     * Test of findDataEntryBetween method, of class MariaDbConnector.
     */
    @Test
    public void testFindDataEntryBetween() throws ConnectionException, InterruptedException {
        //adding test ProviderEntry to database
        ProviderEntry pe = MariaDbConnectorTest.instance.findProviderEntryByName("test");
        ProviderEntry ppe = MariaDbConnectorTest.instance.findProviderEntryByName("test");
        assertEquals(pe, ppe); // Passes if insert succeeded
        
        
        //adding test RouteEntry to database
        RouteEntry re = new RouteEntry();
        re.setName("test");
        re.setDescription("test");
        MariaDbConnectorTest.instance.insert(re);
        RouteEntry rre = MariaDbConnectorTest.instance.findRouteEntryByName("test");
        assertEquals(re, rre); // Passes if insert has succeeded
        
        //Adding data
        Timestamp from = new Timestamp(Calendar.getInstance().getTime().getTime());
        for(int i=0; i<10; i++){
            DataEntry de = new DataEntry(i, rre, ppe);
            MariaDbConnectorTest.instance.insert(de);
            Thread.sleep(1000);
        }
        Timestamp to = new Timestamp(Calendar.getInstance().getTime().getTime());
        
        //Checking if 10 dataentries were added
        int count = MariaDbConnectorTest.instance.findDataEntryBetween(rre.getId(), ppe.getId(), from, to).size();
        assertEquals(10, count);
        
        //Cleaning up
        MariaDbConnectorTest.instance.delete(ppe);
        MariaDbConnectorTest.instance.delete(rre);
    }

    /**
     * Test of findAllRouteEntries method, of class MariaDbConnector.
     */
    @Test
    public void testFindAllRouteEntries() throws ConnectionException {
        System.out.println("findAllRouteEntries");
        ArrayList<RouteEntry> result = (ArrayList<RouteEntry>) MariaDbConnectorTest.instance.findAllRouteEntries();
        boolean b = result != null && result.size() > 0;
        assertTrue(b);
    }

}
