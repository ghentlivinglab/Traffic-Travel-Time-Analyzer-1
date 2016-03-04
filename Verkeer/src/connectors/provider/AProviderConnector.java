/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.provider;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import connectors.database.IDbConnector;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import verkeer.MyLogger;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {

    protected IDbConnector dbConnector;
    protected Collection<RouteEntry> routes;
    protected ProviderEntry providerEntry;

    /**
     * Constructs a new abstract ProviderConnector with an IDbConnector to write
     * data to storage
     *
     * @param dbConnector connector to write DataEntry to
     */
    public AProviderConnector(IDbConnector dbConnector) {
        this.dbConnector = dbConnector;
        this.routes = dbConnector.findAllRouteEntries();
    }

    /**
     * Makes the ProviderConnector fetch data from provider
     */
    public abstract void triggerUpdate();

    /**
     * Saves the generated info to the IDbConnector
     *
     * @param entry
     */
    public void saveToDb(DataEntry entry) {
        dbConnector.insert(entry);
    }
}
