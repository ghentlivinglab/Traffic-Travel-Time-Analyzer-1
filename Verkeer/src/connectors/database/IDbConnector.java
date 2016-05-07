package connectors.database;

import connectors.ProviderEntry;
import connectors.DataEntry;
import connectors.RouteEntry;
import java.sql.Timestamp;
import java.util.Collection;

public interface IDbConnector{
    
    public void insert(DataEntry entry);
    public void insert(RouteEntry entry);
    public void insert(ProviderEntry entry);
    public void delete(DataEntry entry);
    public void delete(RouteEntry entry);
    public void delete(ProviderEntry entry);
    public ProviderEntry    findProviderEntryByName(String name);
    public ProviderEntry    findProviderEntryByID(int id);
    public RouteEntry       findRouteEntryByName(String name);
    public RouteEntry       findRouteEntryByID(int id);
    public DataEntry        findDataEntryByID(int routeId, int providerId, Timestamp timestamp);
    public Collection<DataEntry> findDataEntryBetween(int routeId, int providerId, Timestamp from, Timestamp to);
    public Collection<RouteEntry> findAllRouteEntries();
    
    public void reloadProperties() throws ConnectionException;
    
}
