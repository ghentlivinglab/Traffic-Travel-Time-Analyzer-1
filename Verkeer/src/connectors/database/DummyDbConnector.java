package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyDbConnector implements IDbConnector {

    private Map<Integer, ProviderEntry> providerEntries;
    private List<DataEntry> dataEntries;
    private List<RouteEntry> routeEntries;

    public DummyDbConnector() {
        providerEntries = new HashMap<>();
        dataEntries = new ArrayList<>();
        routeEntries = new ArrayList<>();
        //opvullen van de zogezegde tabellen (routeEntries, providerEntries, dataEntries)
        vulProvidersOp();
        vulRoutesOp();
        vulTrafficDataOp();
    }

    @Override
    public void insert(DataEntry entry) {
        dataEntries.add(entry);
    }

    @Override
    public void insert(RouteEntry entry) {
        routeEntries.add(entry);
    }

    @Override
    public void insert(ProviderEntry entry) {
        providerEntries.put(entry.getId(), entry);
    }

    @Override
    public ProviderEntry findProviderEntryByName(String name) {
        for (ProviderEntry value : providerEntries.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        ProviderEntry n = new ProviderEntry(providerEntries.size(), name);
        return n;
    }

    @Override
    public ProviderEntry findProviderEntryByID(int id) {
        return providerEntries.get(id);
    }

    @Override
    public RouteEntry findRouteEntryByName(String name) {
        RouteEntry ret = null;
        for (RouteEntry value : routeEntries) {
            if (value.getName().equals(name)) {
                ret = value;
            }
        }
        return ret;
    }

    @Override
    public RouteEntry findRouteEntryByID(int id) {
        RouteEntry ret = null;
        for (RouteEntry value : routeEntries) {
            if (value.getId() == id) {
                ret = value;
            }
        }
        return ret;
    }

    @Override
    public DataEntry findDataEntryByID(int routeId, int providerId, Timestamp timestamp) {
        DataEntry det = null;
        for (DataEntry value : dataEntries) {
            if (value.getRoute().getId() == routeId && value.getProvider().getId() == providerId && value.getTimestamp() == timestamp) {
                det = value;
            }
        }
        return det;
    }

    /**
     * Dummy only, for testing
     *
     * @return
     */
    public int getDataEntriesSize() {
        return dataEntries.size();
    }

    @Override
    public Collection<DataEntry> findDataEntryBetween(int routeId, int providerId, Timestamp from, Timestamp to) {
        Collection<DataEntry> dets = new ArrayList<>();
        for (DataEntry value : dataEntries) {
            if (value.getRoute().getId() == routeId && value.getProvider().getId() == providerId
                    && value.getTimestamp().getTime() > from.getTime() && value.getTimestamp().getTime() < to.getTime()) {  // mischien hier met getMinute werken zoals gezegd, maar dit is verouderd
                dets.add(value);
            }
        }
        return dets;
    }

    @Override
    public Collection<RouteEntry> findAllRouteEntries() {
        return routeEntries;
    }

    public void vulRoutesOp() {
        RouteEntry traject = new RouteEntry();
        traject.setName("Rooigemlaan (R40) northbound");
        traject.setDescription("Drongensesteenweg - Palinghuizen");
        traject.setId(27);
        traject.setStartCoordinateLatitude(51.05627181);
        traject.setStartCoordinateLongitude(3.6960078200000006);
        traject.setEndCoordinateLatitude(51.065875);
        traject.setEndCoordinateLongitude(3.699119);
        traject.setLenght(1352);
        routeEntries.add(traject);

        RouteEntry traject2 = new RouteEntry();
        traject2.setId(30);
        traject2.setDescription("E17 - Kennedylaan");
        traject2.setName("Eisenhowerlaan (R4) northbound");
        traject2.setLenght(18823);
        traject2.setStartCoordinateLatitude(51.0137);
        traject2.setStartCoordinateLongitude(3.7182109922981295);
        traject2.setEndCoordinateLatitude(51.08736629632179);
        traject2.setEndCoordinateLongitude(3.7569708390424417);
        routeEntries.add(traject2);
    }

    public void vulProvidersOp() {
        ProviderEntry provider1 = new ProviderEntry();
        provider1.setName("Here");
        provider1.setId(1);
        providerEntries.put(provider1.getId(), provider1);

        ProviderEntry provider2 = new ProviderEntry();
        provider2.setName("Waze");
        provider2.setId(2);
        providerEntries.put(provider2.getId(), provider2);
    }

    public void vulTrafficDataOp() {
        DataEntry det1 = new DataEntry();
        Timestamp date1 = new Timestamp(2016, 02, 28, 15, 50, 45, 999);
        det1.setProvider(providerEntries.get(1)); // provider here
        det1.setRoute(findRouteEntryByID(1));
        det1.setTimestamp(date1);
        det1.setTravelTime(20);
        dataEntries.add(det1);

        DataEntry det2 = new DataEntry();
        Timestamp date2 = new Timestamp(2016, 02, 28, 20, 50, 45, 999);
        det2.setProvider(providerEntries.get(2));// provider waze
        det2.setRoute(findRouteEntryByID(2));
        det2.setTimestamp(date2);
        det2.setTravelTime(25);
        dataEntries.add(det2);
    }

    @Override
    public void reloadProperties() throws ConnectionException {
        
    }

    @Override
    public void delete(DataEntry entry) {
        
    }

    @Override
    public void delete(RouteEntry entry) {
        
    }

    @Override
    public void delete(ProviderEntry entry) {
        
    }

}
