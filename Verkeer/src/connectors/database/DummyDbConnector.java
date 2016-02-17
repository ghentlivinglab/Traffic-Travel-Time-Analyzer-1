/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;

/**
 *
 * @author jarno
 */
public class DummyDbConnector implements IDbConnector{

    @Override
    public void insert(DataEntry entry) {
        
    }

    @Override
    public ProviderEntry findByName(String name) {
        return new ProviderEntry(0, "null");
    }
    
}
