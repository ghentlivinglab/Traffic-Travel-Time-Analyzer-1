/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectors.database;

import connectors.ProviderEntry;
import connectors.DataEntry;
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
    
    private final String INSERT_TTE = "INSERT INTO traveltime (routeID, providerID, traveltime) values ( ?, ?, ?);";
    
    private Connection getConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(connection == null){
                connection = DriverManager.getConnection(URL,USER,PASSWORD);
            }
        }catch (ClassNotFoundException e){
            
        }catch (SQLException e){
            
        }
        return connection;
    }
    
    @Override
    public void insert(DataEntry entry)  {
        try(Connection conn = getConnection()){
            PreparedStatement p = conn.prepareStatement(INSERT_TTE);
        } catch (SQLException ex) {
            
        }
    }

    @Override
    public ProviderEntry findByName(String name) {
        return null;
    }
    
}
