/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.database.DummyDbConnector;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jarno
 */
public class CoyoteProviderConnectorTest {

    public CoyoteProviderConnectorTest() {
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
    public void triggerTest(){
        DummyDbConnector dummy = new DummyDbConnector();
        int voor = dummy.getDataEntriesSize();
        int loops = 1;
        CoyoteProviderConnector connector = new CoyoteProviderConnector(dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+(dummy.getDataEntriesSize()-voor)+" given.");
        }
    }

    @Test
    public void testPerlExecution() {
        /*CoyoteProviderConnector connector = new CoyoteProviderConnector(new DummyDbConnector());
        try {
            connector.runPerl();
        } catch (IOException | InterruptedException e) {
            fail("Perl script failed to run.\n" + e.getLocalizedMessage());
        }
        try {
            File file = new File(connector.getDataFile());
            if(!file.exists()){
                fail("Output file not found.");
            }
            if (!file.delete()) {
                fail("Output could not be deleted.");
            }
        } catch (NullPointerException e) {
            fail("Data file path is null.");

        }*/
    }
}
