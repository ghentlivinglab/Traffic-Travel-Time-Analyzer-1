/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Piet
 */
@Entity
@Table(name = "trafficdata")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Trafficdata.findAll", query = "SELECT t FROM Trafficdata t"),
    @NamedQuery(name = "Trafficdata.findByRouteID", query = "SELECT t FROM Trafficdata t WHERE t.trafficdataPK.routeID = :routeID"),
    @NamedQuery(name = "Trafficdata.findByProviderID", query = "SELECT t FROM Trafficdata t WHERE t.trafficdataPK.providerID = :providerID"),
    @NamedQuery(name = "Trafficdata.findByTimestamp", query = "SELECT t FROM Trafficdata t WHERE t.trafficdataPK.timestamp = :timestamp"),
    @NamedQuery(name = "Trafficdata.findByTraveltime", query = "SELECT t FROM Trafficdata t WHERE t.traveltime = :traveltime")})
public class Trafficdata implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TrafficdataPK trafficdataPK;
    @Column(name = "traveltime")
    private Integer traveltime;
    @JoinColumn(name = "providerID", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Provider provider;
    @JoinColumn(name = "routeID", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Route route;

    public Trafficdata() {
    }

    public Trafficdata(TrafficdataPK trafficdataPK) {
        this.trafficdataPK = trafficdataPK;
    }

    public Trafficdata(int routeID, int providerID, Date timestamp) {
        this.trafficdataPK = new TrafficdataPK(routeID, providerID, timestamp);
    }

    public TrafficdataPK getTrafficdataPK() {
        return trafficdataPK;
    }

    public void setTrafficdataPK(TrafficdataPK trafficdataPK) {
        this.trafficdataPK = trafficdataPK;
    }

    public Integer getTraveltime() {
        return traveltime;
    }

    public void setTraveltime(Integer traveltime) {
        this.traveltime = traveltime;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trafficdataPK != null ? trafficdataPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Trafficdata)) {
            return false;
        }
        Trafficdata other = (Trafficdata) object;
        if ((this.trafficdataPK == null && other.trafficdataPK != null) || (this.trafficdataPK != null && !this.trafficdataPK.equals(other.trafficdataPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entitys.Trafficdata[ trafficdataPK=" + trafficdataPK + " ]";
    }
    
}
