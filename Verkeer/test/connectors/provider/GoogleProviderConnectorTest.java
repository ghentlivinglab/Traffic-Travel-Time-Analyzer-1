package connectors.provider;

import connectors.database.DummyDbConnector;
import static org.junit.Assert.fail;
import org.junit.Test;

public class GoogleProviderConnectorTest {

    public GoogleProviderConnectorTest() {
    }

    @Test
    public void triggerTest() {
        DummyDbConnector dummy = new DummyDbConnector(); // add new dummy database
        int before = dummy.getDataEntriesSize(); // get number of entries before test
        int loops = 1; // run test once
        GoogleProviderConnector connector = new GoogleProviderConnector(dummy); // generate provider

        for (int i = 0; i < loops; i++) {
            connector.triggerUpdate(); // run test (= 1 query per route in the database)
        }
        // Check database count and fail if the wrong number of entries has been added
        if (dummy.getDataEntriesSize() - before != connector.routes.size() * loops) {
            fail("Expected " + (connector.routes.size() * loops) + " dataEntries, " + (dummy.getDataEntriesSize() - before) + " given.");
        }
    }
}
