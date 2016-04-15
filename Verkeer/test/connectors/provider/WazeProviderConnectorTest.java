/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.database.ConnectionException;
import connectors.database.DummyDbConnector;
import connectors.database.MariaDbConnector;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Simon
 */
public class WazeProviderConnectorTest {

    public WazeProviderConnectorTest() {
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

    @Test
    public void triggerTest() throws ConnectionException{
        DummyDbConnector dummy = new DummyDbConnector();
        int voor = dummy.getDataEntriesSize();
        int loops = 1;
        WazeProviderConnector connector = new WazeProviderConnector(dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+(dummy.getDataEntriesSize()-voor)+" given.");
        }
    }
}
