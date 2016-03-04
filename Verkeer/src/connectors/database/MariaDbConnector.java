/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.DataEntry;
import connectors.ProviderEntry;
import connectors.RouteEntry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import verkeer.MyLogger;
import verkeer.Verkeer;

/**
 *
 * @author Robin
 
 */
public class MariaDbConnector implements IDbConnector, MyLogger{
    Connection connection;
    
    public Properties prop;
    
    public MariaDbConnector() {
        try{
            prop = new Properties();
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            if(propsFile == null){
                doLog(Level.WARNING, "database.properties kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e) {
            doLog(Level.WARNING, "database.properties niet gevonden.");
        }catch( IOException ee){
            doLog(Level.WARNING, "database.properties kon niet geladen worden.");
        }
        connection = getConnection();
    }
 
    private Connection getConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(connection == null){
                doLog(Level.INFO, "Connecting to: "+prop.getProperty("URL"));
                connection = DriverManager.getConnection(prop.getProperty("URL"),prop.getProperty("USER"),prop.getProperty("PASSWORD"));
            }
        }catch (ClassNotFoundException e){
            doLog(Level.WARNING, "getConnection() FOUT! ClassNotFoundException");
        }catch (SQLException e){
            doLog(Level.WARNING, "getConnection() FOUT! SQLException");
        }
        System.out.println("\t[Done]\n");
        return connection;
    }
       
    //Insert operations
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
            doLog(Level.WARNING, "het toevoegen van een data entry is mislukt." );
            System.err.println(ex.getMessage());
        }
    }
    @Override
    public void insert(ProviderEntry entry){
        try{
            PreparedStatement p = connection.prepareStatement(prop.getProperty("INSERT_PE"));
            p.setString ( 1, entry.getName());
            p.executeUpdate();
        } catch (SQLException ex) {
            doLog(Level.WARNING, "het toevoegen van een provider entry met naam " + entry.getName() + " is mislukt." );
            System.err.println(ex.getMessage());
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
            doLog(Level.WARNING, "het toevoegen van een route entry met naam " + entry.getName() + " is mislukt." );
            System.err.println(ex.getMessage());
        }
    }
    
    //Select operations
    
    /**
     * Zoekt de ProviderEntry op in de database. Als deze niet bestaat, maakt hij een nieuwe aan en geeft deze terug.
     * @param name
     * @return ProviderEntry uit de database, of een nieuw aangemaakte
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
            doLog(Level.WARNING, "selecteren van een provider entry met providernaam " + name + " is mislukt." );
            System.err.println(ex.getMessage());
        }
        if (ret == null){
            ret = new ProviderEntry();
            ret.setName(name);
            insert(ret);
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
            doLog(Level.WARNING, "selecteren van een provider entry met id " + id + " is mislukt." );
            System.err.println(ex.getMessage());
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
            doLog(Level.WARNING, "selecteren van een route entry met routenaam " + name + " is mislukt." );
            System.err.println(ex.getMessage());;
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
            if(rs.next()){
                ret = new RouteEntry(rs.getString("name"), rs.getDouble("startlat"), rs.getDouble("startlong"), rs.getDouble("endlat"), rs.getDouble("endlong"), rs.getInt("length"), 0);
                ret.setId(id);
            }
            rs.close();
        } catch (SQLException ex) {
            doLog(Level.WARNING, "selecteren van een route met id " + id + " is mislukt." );
            System.err.println(ex.getMessage());
        }
        return ret;
    }
    //Als deep op true is ingesteld worden ook de RouteEntry en ProviderEntry objecten opgehaald en geïnitialiseerd.
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
            doLog(Level.WARNING, "selecteren van een data entry met routeId " + routeId + " en providerId " + providerId + " is mislukt." );
            System.err.println(ex.getMessage());
        }
        return ret;
    }
    @Override
    public DataEntry findDataEntryByID(int routeId, int providerId, Timestamp timestamp) {
        return findDataEntryByID(routeId, providerId, timestamp, false);
    }
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
            doLog(Level.WARNING, "selecteren van alle data entries met routeId " + routeId + " en providerId " + providerId + " van " + from.toString() + " tot " + to.toString() + " mislukt.");
            System.err.println(ex.getMessage());
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
            doLog(Level.WARNING, "selecteren van alle route entries is mislukt.");
            System.err.println(ex.getMessage());
        }
        return ret;
    }

    @Override
    public void doLog(Level lvl, String log){
        try{
            Verkeer.getLogger(MariaDbConnector.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }
    
}
