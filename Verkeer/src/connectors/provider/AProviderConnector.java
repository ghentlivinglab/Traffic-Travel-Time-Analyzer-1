/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import java.util.List;
import connectors.database.IDbConnector;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import connectors.DataEntry;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {
    protected IDbConnector dbConnector;
    protected List<RouteEntry> trajecten;
    protected ProviderEntry providerEntry;
    
    public AProviderConnector(List<RouteEntry> trajecten, IDbConnector dbConnector){
        this.trajecten = trajecten;
        this.dbConnector = dbConnector;
    }
    public abstract void triggerUpdate();
    public void saveToDb(DataEntry entry){
        dbConnector.insert(entry);
    }
}
