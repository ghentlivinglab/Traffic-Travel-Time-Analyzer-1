package connectors.provider;

import connectors.database.DummyDbConnector;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CoyoteProviderConnectorTest {

    public CoyoteProviderConnectorTest() {
    }

    @Test
    public void triggerTest() {
        DummyDbConnector dummy = new DummyDbConnector();
        int voor = dummy.getDataEntriesSize();
        int loops = 1;
        CoyoteProviderConnector connector = new CoyoteProviderConnector(dummy);

        for (int i = 0; i < loops; i++) {
            connector.triggerUpdate();
        }
        // Check database count
        if (dummy.getDataEntriesSize() - voor != connector.routes.size() * loops) {
            fail("Expected " + (connector.routes.size() * loops) + " dataEntries, " + (dummy.getDataEntriesSize() - voor) + " given.");
        }
    }
}
