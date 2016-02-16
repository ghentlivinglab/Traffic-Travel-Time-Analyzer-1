/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.ProviderEntry;
import connectors.TravelTimeEntry;

/**
 *
 * @author jarno
 */
public class DummyDbConnector implements IDbConnector{

    @Override
    public void storeInDb(TravelTimeEntry entry) {
        
    }

    @Override
    public ProviderEntry getProvider(String name) {
        return new ProviderEntry(0, "null");
    }
    
}
