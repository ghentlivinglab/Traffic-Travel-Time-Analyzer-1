/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.provider.AProviderConnector;
import java.util.List;
import connectors.database.IDbConnector;
import connectors.RouteEntry;

/**
 *
 * @author Simon
 */
public class HereProviderConnector extends AProviderConnector {
    
    public HereProviderConnector(List<RouteEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        String providerName = "Here";
        this.providerEntry = dbConnector.findByName(providerName);
    }
    
    @Override
    public void triggerUpdate(){
        // TODO
    }
}
