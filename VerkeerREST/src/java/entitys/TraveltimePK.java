/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Piet
 */
@Embeddable
public class TraveltimePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "routeID")
    private int routeID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "providerID")
    private int providerID;

    public TraveltimePK() {
    }

    public TraveltimePK(int routeID, int providerID) {
        this.routeID = routeID;
        this.providerID = providerID;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public int getProviderID() {
        return providerID;
    }

    public void setProviderID(int providerID) {
        this.providerID = providerID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) routeID;
        hash += (int) providerID;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TraveltimePK)) {
            return false;
        }
        TraveltimePK other = (TraveltimePK) object;
        if (this.routeID != other.routeID) {
            return false;
        }
        if (this.providerID != other.providerID) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entitys.TraveltimePK[ routeID=" + routeID + ", providerID=" + providerID + " ]";
    }
    
}
