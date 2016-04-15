package connectors.provider;

import connectors.database.ConnectionException;
import connectors.database.DummyDbConnector;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HereProviderConnectorTest {

    public HereProviderConnectorTest() {
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
        HereProviderConnector connector = new HereProviderConnector(dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+(dummy.getDataEntriesSize()-voor)+" given.");
        }
    }
}
