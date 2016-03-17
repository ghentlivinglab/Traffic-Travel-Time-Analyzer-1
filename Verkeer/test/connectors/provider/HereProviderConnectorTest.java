package connectors.provider;

import connectors.DataEntry;
import connectors.database.ConnectionException;
import connectors.database.DummyDbConnector;
import connectors.database.IDbConnector;
import connectors.database.MariaDbConnector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void returnTest() throws ConnectionException {
        IDbConnector db = new MariaDbConnector();
        HereProviderConnector connector = new HereProviderConnector(db);
        connector.triggerUpdate();

        // Wait for all threads to complete, read their return data (= DataEntry)
        for (Future<DataEntry> hashRequest : connector.buzyRequests) {
            try {
                DataEntry data = hashRequest.get();
                if (data == null) {
                    fail("DataEntry is null");
                }
            } catch (InterruptedException ex) {
                //Logger.getLogger(HereProviderConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
                // Logger logs zijn veel te onduidelijk. Even uitgecomment.
                fail("ConnectionTest Failed: InterruptedException");
            } catch (ExecutionException ex) {
                //Logger.getLogger(HereProviderConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
                // Logger logs zijn veel te onduidelijk. Even uitgecomment.
                System.out.println(ex.getCause().getCause().getMessage());
                fail("ExecutionException");
            }
        }
    }

    @Test
    public void insertDatabaseTest() throws InterruptedException, ExecutionException {
        DummyDbConnector dummy = new DummyDbConnector();
        int loops = 0;
        int voor = dummy.getDataEntriesSize();
        HereProviderConnector connector = new HereProviderConnector(dummy);

        for (int i = 0; i < loops; i++) {
            connector.triggerUpdate();
            // Wait for all threads to complete
            for (Future<DataEntry> hashRequest : connector.buzyRequests) {
                hashRequest.get();
            }
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size() * loops) {
            fail("Expected " + (connector.routes.size() * loops) + " dataEntries, " + (dummy.getDataEntriesSize()-voor) + " given.");
        }
    }
}
