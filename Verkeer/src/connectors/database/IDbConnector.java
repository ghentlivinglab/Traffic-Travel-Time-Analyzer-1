/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.*;
import java.sql.Date;

/**
 *
 * @author Simon
 */
public interface IDbConnector {
    public void insert(DataEntry entry);
    public void insert(RouteEntry entry);
    public void insert(ProviderEntry entry);
    public ProviderEntry    findProviderEntryByName(String name);
    public ProviderEntry    findProviderEntryByID(int id);
    public RouteEntry       findRouteEntryByName(String name);
    public RouteEntry       findRouteEntryByID(int id);
    public DataEntry        findDataEntryByID(int routeId, int providerId, Date timestamp);
}
