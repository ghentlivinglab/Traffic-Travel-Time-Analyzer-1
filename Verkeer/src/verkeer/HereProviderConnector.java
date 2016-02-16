/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import java.util.List;

/**
 *
 * @author Simon
 */
public class HereProviderConnector extends AProviderConnector {
    
    public HereProviderConnector(List<TrajectEntry> trajecten, IDbConnector dbConnector) {
        super(trajecten, dbConnector);
        String providerName = "Here";
        this.providerEntry = dbConnector.getProvider(providerName);
    }

    public void triggerUpdate(){
        // TODO
    }
}
