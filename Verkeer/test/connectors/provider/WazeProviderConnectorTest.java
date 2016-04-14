/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
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
    public void returnTest() throws ConnectionException{
        IDbConnector db = new MariaDbConnector();
        WazeProviderConnector connector = new WazeProviderConnector(db);
        
        AsyncHttpClientConfig.Builder ab = new AsyncHttpClientConfig.Builder();
        ab.setMaxConnections(15);
        AsyncHttpClient a = new AsyncHttpClient(ab.build());
        
        connector.triggerUpdate(a);

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
        int voor = dummy.getDataEntriesSize();
        int loops = 1;
        WazeProviderConnector connector = new WazeProviderConnector(dummy);
        
        AsyncHttpClientConfig.Builder ab = new AsyncHttpClientConfig.Builder();
        ab.setMaxConnections(15);
        AsyncHttpClient a = new AsyncHttpClient(ab.build());
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate(a);
            // Wait for all threads to complete
            for (Future<Boolean> hashRequest : connector.buzyRequests) {
                hashRequest.get();
            }
        }
        // Check database count
        if (dummy.getDataEntriesSize()-voor != connector.routes.size()*loops){
            fail("Expected "+(connector.routes.size()*loops)+" dataEntries, "+(dummy.getDataEntriesSize()-voor)+" given.");
        }
    }

}
