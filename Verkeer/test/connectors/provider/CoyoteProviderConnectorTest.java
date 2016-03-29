/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.database.DummyDbConnector;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void test() {
        CoyoteProviderConnector connector = new CoyoteProviderConnector(new DummyDbConnector());
        connector.triggerUpdate();
    }

    @Test
    public void testPerlExecution() {
        CoyoteProviderConnector connector = new CoyoteProviderConnector(new DummyDbConnector());
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

        }
    }
}
