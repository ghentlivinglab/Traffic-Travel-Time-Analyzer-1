/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import org.junit.After;
import org.junit.AfterClass;
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
    public void test() {
        /*AsyncHttpClientConfig.Builder ab = new AsyncHttpClientConfig.Builder();
        ab.setMaxConnections(15);
        AsyncHttpClient a = new AsyncHttpClient(ab.build());
        CoyoteProviderConnector connector = new CoyoteProviderConnector(new DummyDbConnector());
        connector.triggerUpdate(a);*/
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
