/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.ProviderEntry;
import connectors.DataEntry;
import connectors.RouteEntry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Robin
 */
public class MariaDbConnector implements IDbConnector{
    Connection connection = null;
    
    private final String USER = "test";
    private final String PASSWORD = "test";
    private final String IP = "192.168.1.103";
    private final String PORT = "3306";
    private final String DATABASE = "verkeer1";
    private final String URL = "jdbc:mariadb://"+IP+":"+PORT+"/"+DATABASE;
    
    private final String INSERT_DE = "INSERT INTO trafficdata (routeID, providerID, timestamp, traveltime) values ( ?, ?, ?, ?);";
    private final String INSERT_PE = "INSERT INTO providers (id, name) values ( ?, ?);";
    private final String INSERT_RE = "INSERT INTO routesINSERT_DE (id, length, name, startlat, startlong, endlat, endlong) values ( ?, ?, ?, ?, ?, ?, ?);";
    
    private Connection getConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(connection == null){
                connection = DriverManager.getConnection(URL,USER,PASSWORD);
            }
        }catch (ClassNotFoundException e){
            
        }catch (SQLException e){
            System.err.println("getConnection() FOUT!");
        }
        return connection;
    }
    
    @Override
    public void insert(DataEntry entry)  {
        try(Connection conn = getConnection()){
            PreparedStatement p = conn.prepareStatement(INSERT_DE);
            p.setInt( 1, entry.getProvider().getId());
            p.setInt( 2, entry.getRoute().getId());
            p.setDate(3, entry.getTimestamp());
            p.setDouble(4, entry.getTravelTime());
            p.executeQuery();
        } catch (SQLException ex) {
            System.err.println("insert DataEntry FOUT!");
        }
    }
    public void insert(ProviderEntry entry){
        try(Connection conn = getConnection()){
            PreparedStatement p = conn.prepareStatement(INSERT_PE);
            p.setInt    ( 1, entry.getId());
            p.setString ( 2, entry.getName());
            p.executeQuery();
        } catch (SQLException ex) {
            System.err.println("insert DataEntry FOUT!");
        }
    }
    public void insert(RouteEntry entry){
        try(Connection conn = getConnection()){
            PreparedStatement p = conn.prepareStatement(INSERT_RE);
            p.setInt    ( 1, entry.getId());
            p.setInt    ( 2, entry.getLenght());
            p.setString (3, entry.getName());
            p.setDouble (4, entry.getStartCoordinateLatitude());
            p.setDouble (5, entry.getStartCoordinateLongitude());
            p.setDouble (6, entry.getEndCoordinateLatitude());
            p.setDouble (7, entry.getEndCoordinateLongitude());
            p.executeQuery();
        } catch (SQLException ex) {
            System.err.println("insert DataEntry FOUT!");
        }
    }
    
    @Override
    public ProviderEntry findByName(String name) {
        return null;
    }
    
}
