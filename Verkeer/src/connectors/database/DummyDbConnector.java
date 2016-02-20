/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import java.sql.Date;

/**
 *
 * @author jarno
 */
public class DummyDbConnector implements IDbConnector{

    @Override
    public void insert(DataEntry entry) {
        
    }

    @Override
    public void insert(RouteEntry entry) {
       
    }

    @Override
    public void insert(ProviderEntry entry) {
        
    }

    @Override
    public ProviderEntry findProviderEntryByName(String name) {
        return new ProviderEntry();
    }

    @Override
    public ProviderEntry findProviderEntryByID(int id) {
       return new ProviderEntry();
    }

    @Override
    public RouteEntry findRouteEntryByName(String name) {
        return new RouteEntry();
    }

    @Override
    public RouteEntry findRouteEntryByID(int id) {
        return new RouteEntry();
    }

    @Override
    public DataEntry findDataEntryByID(int routeId, int providerId, Date timestamp) {
        return new DataEntry();
    }
    
}
