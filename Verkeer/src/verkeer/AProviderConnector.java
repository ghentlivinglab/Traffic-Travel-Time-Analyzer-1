/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import java.util.List;

/**
 *
 * @author Simon
 */
public abstract class AProviderConnector {
    private IDbConnector dbConnector;
    private List<TrajectEntry> trajecten;
    private int providerId;
    
    public AProviderConnector(List<TrajectEntry> trajecten, IDbConnector dbConnector){
        this.trajecten = trajecten;
        this.dbConnector = dbConnector;
    }
    public abstract void triggerUpdate();
    public void saveToDb(TravelTimeEntry entry){
        dbConnector.storeInDb(entry);
    }
}
