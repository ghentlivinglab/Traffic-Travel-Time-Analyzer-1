/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import java.sql.Date;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author HP
 */
public class MariaDbConnectorTest {
    
    public MariaDbConnectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of insert method, of class MariaDbConnector.
     */
    @Test
    public void testInsert_DataEntry() {
        System.out.println("insert");
        DataEntry entry = null;
        MariaDbConnector instance = new MariaDbConnector();
        instance.insert(entry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insert method, of class MariaDbConnector.
     */
    @Test
    public void testInsert_ProviderEntry() {
        System.out.println("insert");
        ProviderEntry entry = null;
        MariaDbConnector instance = new MariaDbConnector();
        instance.insert(entry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insert method, of class MariaDbConnector.
     */
    @Test
    public void testInsert_RouteEntry() {
        System.out.println("insert");
        RouteEntry entry = null;
        MariaDbConnector instance = new MariaDbConnector();
        instance.insert(entry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findProviderEntryByName method, of class MariaDbConnector.
     */
    @Test
    public void testFindProviderEntryByName() {
        System.out.println("findProviderEntryByName");
        String name = "";
        MariaDbConnector instance = new MariaDbConnector();
        ProviderEntry expResult = null;
        ProviderEntry result = instance.findProviderEntryByName(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findProviderEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindProviderEntryByID() {
        System.out.println("findProviderEntryByID");
        int id = 0;
        MariaDbConnector instance = new MariaDbConnector();
        ProviderEntry expResult = null;
        ProviderEntry result = instance.findProviderEntryByID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findRouteEntryByName method, of class MariaDbConnector.
     */
    @Test
    public void testFindRouteEntryByName() {
        System.out.println("findRouteEntryByName");
        String name = "";
        MariaDbConnector instance = new MariaDbConnector();
        RouteEntry expResult = null;
        RouteEntry result = instance.findRouteEntryByName(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findRouteEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindRouteEntryByID() {
        System.out.println("findRouteEntryByID");
        int id = 0;
        MariaDbConnector instance = new MariaDbConnector();
        RouteEntry expResult = null;
        RouteEntry result = instance.findRouteEntryByID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findDataEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindDataEntryByID_4args() {
        System.out.println("findDataEntryByID");
        int routeId = 0;
        int providerId = 0;
        Date timestamp = null;
        boolean deep = false;
        MariaDbConnector instance = new MariaDbConnector();
        DataEntry expResult = null;
        DataEntry result = instance.findDataEntryByID(routeId, providerId, timestamp, deep);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findDataEntryByID method, of class MariaDbConnector.
     */
    @Test
    public void testFindDataEntryByID_3args() {
        System.out.println("findDataEntryByID");
        int routeId = 0;
        int providerId = 0;
        Date timestamp = null;
        MariaDbConnector instance = new MariaDbConnector();
        DataEntry expResult = null;
        DataEntry result = instance.findDataEntryByID(routeId, providerId, timestamp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findDataEntryBetween method, of class MariaDbConnector.
     */
    @Test
    public void testFindDataEntryBetween() {
        System.out.println("findDataEntryBetween");
        int routeId = 0;
        int providerId = 0;
        Date from = null;
        Date to = null;
        MariaDbConnector instance = new MariaDbConnector();
        Collection<DataEntry> expResult = null;
        Collection<DataEntry> result = instance.findDataEntryBetween(routeId, providerId, from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findAllRouteEntries method, of class MariaDbConnector.
     */
    @Test
    public void testFindAllRouteEntries() {
        System.out.println("findAllRouteEntries");
        MariaDbConnector instance = new MariaDbConnector();
        Collection<RouteEntry> expResult = null;
        Collection<RouteEntry> result = instance.findAllRouteEntries();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
