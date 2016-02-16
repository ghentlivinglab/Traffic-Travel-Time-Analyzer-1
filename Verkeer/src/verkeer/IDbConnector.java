/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

/**
 *
 * @author Simon
 */
public interface IDbConnector {
    public void storeInDb(TravelTimeEntry entry);
    public ProviderEntry getProvider(String name);
}
