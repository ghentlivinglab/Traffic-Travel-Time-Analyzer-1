/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.ProviderEntry;
import connectors.DataEntry;
import connectors.RouteEntry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 *
 * @author Robin
 
 */
public class MariaDbConnector implements IDbConnector{
    Connection connection;
    
    public Properties prop;
    public String conn_str;
    
    /** 
     * Creates a new instance of MariaDbConnector.
     * The connection parameters can be changed by modifying the properties file: connectors/database/database.properties
     * @throws connectors.database.ConnectionException
     */
    public MariaDbConnector() throws ConnectionException {
        try{
            prop = new Properties();
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            if(propsFile == null){
                System.err.println("connectors/database/database.properties kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e){
            System.err.println("connectors/database/database.properties niet gevonden.");
        }catch( IOException ee){
            System.err.println("connectors/database/database.properties kon niet geladen worden.");
        }
        initConnectionURL();
        initConnection();
    }
    /**
     * Initializes the connection string with the parameters from the properties file.
     */
    private void initConnectionURL(){
        StringBuilder sb = new StringBuilder();
        sb.append(prop.getProperty("PREFIX"));
        sb.append(prop.getProperty("IP"));
        sb.append(":");
        sb.append(prop.getProperty("PORT"));
        sb.append("/");
        sb.append(prop.getProperty("DATABASE"));
        conn_str=sb.toString();
    }
    /**
     * Initializes the Connection object.
     * If no connection could be established, a ConnectionException is thrown.
     */
    private void initConnection() throws ConnectionException{
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(connection == null){
                System.out.print("Connecting to: "+conn_str);
                connection = DriverManager.getConnection(conn_str,prop.getProperty("USER"),prop.getProperty("PASSWORD"));
            }
        }catch (ClassNotFoundException e){
            System.out.println("\n SQL Driver could not be loaded. Check all libraries are provided.");
        }catch (SQLException e){
            throw new ConnectionException();
        }
        System.out.println("\t[Done]\n");
    }
       
    /**
     * Inserts a DataEntry object in the database
     * @param entry The DataEntry to insert in de database
     */
    @Override
    public void insert(DataEntry entry)  {
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_DE"));
            p.setInt( 1, entry.getRoute().getId());
            p.setInt( 2, entry.getProvider().getId());
            p.setTimestamp(3, entry.getTimestamp());
            p.setInt(4, entry.getTravelTime());
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Inserts a ProviderEntry object in the database
     * @param entry The ProviderEntry to insert in de database
     */
    @Override
    public void insert(ProviderEntry entry){
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_PE"));
            p.setString ( 1, entry.getName());
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Inserts a RouteEntry object in the database
     * @param entry The RouteEntry to insert in de database
     */
    @Override
    public void insert(RouteEntry entry){
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_RE"));
            p.setInt    (1, entry.getLenght());
            p.setString (2, entry.getName());
            p.setDouble (3, entry.getStartCoordinateLatitude());
            p.setDouble (4, entry.getStartCoordinateLongitude());
            p.setDouble (5, entry.getEndCoordinateLatitude());
            p.setDouble (6, entry.getEndCoordinateLongitude());
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    //Select operations
    
    /**
     * Finds a provider based on its name. When no entry is found, a new entry is inserted in the database.
     * @param name that identifies the provider.
     * @return ProviderEntry that was found or inserted.
     */
    @Override
    public ProviderEntry findProviderEntryByName(String name) {
        ProviderEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_PE_NAME"));
            p.setString(1, name);
            ResultSet rs = p.executeQuery();
            if(rs.next())
                ret = new ProviderEntry(rs.getInt("id"), rs.getString("name"));
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (ret == null){
            ret = new ProviderEntry();
            ret.setName(name);
            insert(ret);
        }
        return ret;
    }
    /**
     * Finds a provider based on its name. When no entry is found, nothing is inserted in the database.
     * (in contrast to findProviderEntryByName)
     * @param id that identifies the provider.
     * @return ProviderEntry that was found or null if no provider exists with this name.
     */
    @Override
    public ProviderEntry findProviderEntryByID(int id) {
        ProviderEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_PE_ID"));
            p.setInt(1, id);
            ResultSet rs = p.executeQuery();
            if(rs.next())
                ret = new ProviderEntry(rs.getInt("id"), rs.getString("name"));
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    /**
     * Finds a route based on its name.
     * @param name that identifies the route.
     * @return RouteEntry that was found.
     */
    @Override
    public RouteEntry findRouteEntryByName(String name) {
        RouteEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_RE_NAME"));
            p.setString(1, name);
            ResultSet rs = p.executeQuery();
            if(rs.next())
                ret = new RouteEntry(rs.getString("name"), rs.getDouble("startlat"), rs.getDouble("startlong"), rs.getDouble("endlat"), rs.getDouble("endlong"), rs.getInt("length"), 0);
            //TODO: Als er niks wordt teruggegeven, dan wordt er eentje aangemaakt. Analoog zoals bij provider.
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    /**
     * Finds a route based on its id.
     * @param id that identifies the route.
     * @return RouteEntry that was found.
     */
    @Override
    public RouteEntry findRouteEntryByID(int id) {
        RouteEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_RE_ID"));
            p.setInt(1, id);
            ResultSet rs = p.executeQuery();
            if(rs.next()){
                ret = new RouteEntry(rs.getString("name"), rs.getDouble("startlat"), rs.getDouble("startlong"), rs.getDouble("endlat"), rs.getDouble("endlong"), rs.getInt("length"), 0);
                ret.setId(id);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    /**
     * Finds the traveltime for a certain route, provider and time.
     * @param routeId that identifies the route
     * @param providerId that identifies the provider
     * @param timestamp on which this data was recorded
     * @param deep whether or not the ProviderEntry and RouteEntry objects should be initialized.
     * @return DataEntry
     */
    public DataEntry findDataEntryByID(int routeId, int providerId, Timestamp timestamp, boolean deep) {
        DataEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_DE"));
            p.setInt(1, routeId);
            p.setInt(2, providerId);
            p.setTimestamp(3, timestamp);
            ResultSet rs = p.executeQuery();
            if(rs.next()){
                ret = new DataEntry();
                ret.setTravelTime(rs.getInt("traveltime"));
                ret.setTimestamp(rs.getTimestamp("timestamp"));
                if(deep){
                    RouteEntry route = findRouteEntryByID(routeId);
                    ProviderEntry prov = findProviderEntryByID(providerId);
                    if(route != null) ret.setRoute(route);
                    if(prov != null) ret.setProvider(prov);
                }
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    /**
     * Finds the traveltime for a certain route, provider and time.
     * If the RouteEntry and ProviderEntry references need to be initialized use findDataEntryByID(int routeId, int providerId, Date timestamp, boolean deep)
     * @param routeId that identifies the route
     * @param providerId that identifies the provider
     * @param timestamp on which this data was recorded
     * @return DataEntry
     */
    @Override
    public DataEntry findDataEntryByID(int routeId, int providerId, Timestamp timestamp) {
        return findDataEntryByID(routeId, providerId, timestamp, false);
    }
    /**
     * Finds all the DataEntry's for a certain route and provider who were recorded between two timestamps.
     * Both timestamps are included.
     * @param routeId that identifies the route
     * @param providerId that identifies the provider
     * @param from 
     * @param to
     * @return Collection of DataEntry objects
     */
    @Override
    public Collection<DataEntry> findDataEntryBetween(int routeId, int providerId, Timestamp from, Timestamp to){
        ArrayList<DataEntry> ret = new ArrayList<>();
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_DE_BETWEEN"));
            p.setInt(1, routeId);
            p.setInt(2, providerId);
            p.setTimestamp(3, from);
            p.setTimestamp(4, to);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                DataEntry d = new DataEntry();
                d.setTimestamp(rs.getTimestamp("timestamp"));
                d.setTravelTime(rs.getInt("traveltime"));
                ret.add(d);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    /**
     * Finds all routes stored in the database
     * @return Collection of RouteEntry objects
     */
    public Collection<RouteEntry> findAllRouteEntries(){
        Collection<RouteEntry> ret = new ArrayList<>();
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_RE"));
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                RouteEntry r = new RouteEntry();
                r.setId(rs.getInt("id"));
                r.setLenght(rs.getInt("length"));
                r.setName(rs.getString("name"));
                r.setStartCoordinateLatitude(rs.getDouble("startlat"));
                r.setStartCoordinateLongitude(rs.getDouble("startlong"));
                r.setEndCoordinateLatitude(rs.getDouble("endlat"));
                r.setEndCoordinateLongitude(rs.getDouble("endlong"));
                ret.add(r);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    
}
