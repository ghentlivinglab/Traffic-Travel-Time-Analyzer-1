/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.DataEntry;
import connectors.RouteEntry;
import connectors.database.DummyDbConnector;
import java.util.ArrayList;
import java.util.List;
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
public class HereProviderConnectorTest {
    static List<RouteEntry> trajecten;

    public HereProviderConnectorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        trajecten = new ArrayList<>();

        RouteEntry traject = new RouteEntry();
        traject.setName("R40 Drongensesteenweg -> Palinghuizen");
        traject.setStartCoordinateLatitude(51.0560905);
        traject.setStartCoordinateLongitude(3.6951634);
        traject.setEndCoordinateLatitude(51.0663037);
        traject.setEndCoordinateLongitude(3.6996797);
        trajecten.add(traject);
        
        traject = new RouteEntry();
        traject.setName("R40 Drongensesteenweg -> Palinghuizen - kopie");
        traject.setStartCoordinateLatitude(51.0560905);
        traject.setStartCoordinateLongitude(3.6951634);
        traject.setEndCoordinateLatitude(51.0663037);
        traject.setEndCoordinateLongitude(3.6996797);
        trajecten.add(traject);
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
    public void returnTest(){
        HereProviderConnector connector = new HereProviderConnector(trajecten, new DummyDbConnector());
        connector.triggerUpdate();
        
        // Wait for all threads to complete, read their return data (= DataEntry)
        for (Future<DataEntry> hashRequest : connector.buzyRequests) {
            try {
                DataEntry data = hashRequest.get();
                if (data == null){
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
    public void insertDatabaseTest() throws InterruptedException, ExecutionException{
        DummyDbConnector dummy = new DummyDbConnector();
        int loops = 4;
        HereProviderConnector connector = new HereProviderConnector(trajecten, dummy);
        
        for (int i = 0; i<loops; i++){
            connector.triggerUpdate();
            // Wait for all threads to complete
            for (Future<DataEntry> hashRequest : connector.buzyRequests) {
                hashRequest.get();
            }
        }
        // Check database count
        if (dummy.getDataEntriesSize() != trajecten.size()*loops){
            fail("Expected "+(trajecten.size()*loops)+" dataEntries, "+dummy.getDataEntriesSize()+" given.");
        }
    }
}
