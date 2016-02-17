/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.ProviderEntry;
import connectors.DataEntry;

/**
 *
 * @author Simon
 */
public interface IDbConnector {
    public void storeInDb(DataEntry entry);
    public ProviderEntry getProvider(String name);
}
