/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Piet
 */
@Embeddable
public class TrafficdataPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "routeID")
    private int routeID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "providerID")
    private int providerID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public TrafficdataPK() {
    }

    public TrafficdataPK(int routeID, int providerID, Date timestamp) {
        this.routeID = routeID;
        this.providerID = providerID;
        this.timestamp = timestamp;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) routeID;
        hash += (int) providerID;
        hash += (timestamp != null ? timestamp.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrafficdataPK)) {
            return false;
        }
        TrafficdataPK other = (TrafficdataPK) object;
        if (this.routeID != other.routeID) {
            return false;
        }
        if (this.providerID != other.providerID) {
            return false;
        }
        if ((this.timestamp == null && other.timestamp != null) || (this.timestamp != null && !this.timestamp.equals(other.timestamp))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entitys.TrafficdataPK[ routeID=" + routeID + ", providerID=" + providerID + ", timestamp=" + timestamp + " ]";
    }
    
}
