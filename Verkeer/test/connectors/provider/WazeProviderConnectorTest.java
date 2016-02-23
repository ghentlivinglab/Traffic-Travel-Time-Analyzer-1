/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.DataEntry;
import connectors.database.DummyDbConnector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void returnTest() {
        WazeProviderConnector connector = new WazeProviderConnector(new DummyDbConnector());
        connector.triggerUpdate();

        // Wait for all threads to complete, read their return data (= DataEntry)
        for (Future<Boolean> hashRequest : connector.buzyRequests) {
            try {
                Boolean data = hashRequest.get();
                if (!data){
                    fail("Request mislukt");
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
    public void insertDatabaseTest() throws InterruptedException, ExecutionException{
        DummyDbConnector dummy = new DummyDbConnector();
        int loops = 0;
        WazeProviderConnector connector = new WazeProviderConnector(dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
            // Wait for all threads to complete
            for (Future<Boolean> hashRequest : connector.buzyRequests) {
                hashRequest.get();
            }
        }
        // Check database count
        if (dummy.getDataEntriesSize() != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+dummy.getDataEntriesSize()+" given.");
        }
    }

}
