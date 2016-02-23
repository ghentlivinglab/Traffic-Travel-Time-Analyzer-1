/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.database.IDbConnector;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import connectors.DataEntry;
import java.util.Collection;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {
    protected IDbConnector dbConnector;
    protected Collection<RouteEntry> trajecten;
    protected ProviderEntry providerEntry;
    
    public AProviderConnector(IDbConnector dbConnector){
        this.dbConnector = dbConnector;
        this.trajecten = dbConnector.findAllRouteEntries();
    }
    public abstract void triggerUpdate();
    public void saveToDb(DataEntry entry){
        dbConnector.insert(entry);
    }
}
