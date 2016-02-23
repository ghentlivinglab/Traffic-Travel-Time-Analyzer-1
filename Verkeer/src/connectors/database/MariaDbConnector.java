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
import java.sql.Date;
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
    
    public MariaDbConnector() {
        try{
            prop = new Properties();
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            if(propsFile == null){
                System.err.println("database.properties kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e){
            System.err.println("database.properties niet gevonden.");
        }catch( IOException ee){
            System.err.println("database.properties kon niet geladen worden.");
        }
        connection = getConnection();
    }
 
    private Connection getConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(connection == null){
                System.out.print("Connecting to: "+prop.getProperty("URL"));
                connection = DriverManager.getConnection(prop.getProperty("URL"),prop.getProperty("USER"),prop.getProperty("PASSWORD"));
            }
        }catch (ClassNotFoundException e){
            System.out.println("getConnection() FOUT! ClassNotFoundException");
        }catch (SQLException e){
            System.err.println("getConnection() FOUT! SQLException");
        }
        System.out.println("\t[Done]\n");
        return connection;
    }
       
    //Insert operations
    @Override
    public void insert(DataEntry entry)  {
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_DE"));
            p.setInt( 1, entry.getProvider().getId());
            p.setInt( 2, entry.getRoute().getId());
            p.setDate(3, entry.getTimestamp());
            p.setDouble(4, entry.getTravelTime());
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void insert(ProviderEntry entry){
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_PE"));
            p.setInt    ( 1, entry.getId());
            p.setString ( 2, entry.getName());
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
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
        return ret;
    }
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
    //Opgelet! Het veld traveltime is hier niet ingevuld.
    // Indien niet gevonden wordt er null teruggegeven.
    @Override
    public RouteEntry findRouteEntryByName(String name) {
        RouteEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_RE_NAME"));
            p.setString(1, name);
            ResultSet rs = p.executeQuery();
            if(rs.next())
                ret = new RouteEntry(rs.getString("name"), rs.getDouble("startlat"), rs.getDouble("startlong"), rs.getDouble("endlat"), rs.getDouble("endlong"), rs.getInt("length"), 0);
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    @Override
    public RouteEntry findRouteEntryByID(int id) {
        RouteEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_RE_ID"));
            p.setInt(1, id);
            ResultSet rs = p.executeQuery();
            if(rs.next())
                ret = new RouteEntry(rs.getString("name"), rs.getDouble("startlat"), rs.getDouble("startlong"), rs.getDouble("endlat"), rs.getDouble("endlong"), rs.getInt("length"), 0);
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    //Als deep op true is ingesteld worden ook de RouteEntry en ProviderEntry objecten opgehaald en geïnitialiseerd.
    public DataEntry findDataEntryByID(int routeId, int providerId, Date timestamp, boolean deep) {
        DataEntry ret = null;
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_DE"));
            p.setInt(1, routeId);
            p.setInt(2, providerId);
            p.setDate(3, timestamp);
            ResultSet rs = p.executeQuery();
            if(rs.next()){
                ret = new DataEntry();
                ret.setTravelTime(rs.getInt("traveltime"));
                ret.setTimestamp(rs.getDate("timestamp"));
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
    @Override
    public DataEntry findDataEntryByID(int routeId, int providerId, Date timestamp) {
        return findDataEntryByID(routeId, providerId, timestamp, false);
    }
    @Override
    public Collection<DataEntry> findDataEntryBetween(int routeId, int providerId, Date from, Date to){
        ArrayList<DataEntry> ret = new ArrayList<>();
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("SELECT_DE_BETWEEN"));
            p.setInt(1, routeId);
            p.setInt(2, providerId);
            p.setDate(3, from);
            p.setDate(4, to);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                DataEntry d = new DataEntry();
                d.setTimestamp(rs.getDate("timestamp"));
                d.setTravelTime(rs.getInt("traveltime"));
                ret.add(d);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
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
