/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys;

import java.io.Serializable;
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
@Table(name = "traveltimes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Traveltime.findAll", query = "SELECT t FROM Traveltime t"),
    @NamedQuery(name = "Traveltime.findByRouteID", query = "SELECT t FROM Traveltime t WHERE t.traveltimePK.routeID = :routeID"),
    @NamedQuery(name = "Traveltime.findByProviderID", query = "SELECT t FROM Traveltime t WHERE t.traveltimePK.providerID = :providerID"),
    @NamedQuery(name = "Traveltime.findByTraveltime", query = "SELECT t FROM Traveltime t WHERE t.traveltime = :traveltime")})
public class Traveltime implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TraveltimePK traveltimePK;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "traveltime")
    private Double traveltime;
    @JoinColumn(name = "providerID", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Provider provider;
    @JoinColumn(name = "routeID", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Route route;

    public Traveltime() {
    }

    public Traveltime(TraveltimePK traveltimePK) {
        this.traveltimePK = traveltimePK;
    }

    public Traveltime(int routeID, int providerID) {
        this.traveltimePK = new TraveltimePK(routeID, providerID);
    }

    public TraveltimePK getTraveltimePK() {
        return traveltimePK;
    }

    public void setTraveltimePK(TraveltimePK traveltimePK) {
        this.traveltimePK = traveltimePK;
    }

    public Double getTraveltime() {
        return traveltime;
    }

    public void setTraveltime(Double traveltime) {
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
        hash += (traveltimePK != null ? traveltimePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Traveltime)) {
            return false;
        }
        Traveltime other = (Traveltime) object;
        if ((this.traveltimePK == null && other.traveltimePK != null) || (this.traveltimePK != null && !this.traveltimePK.equals(other.traveltimePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entitys.Traveltime[ traveltimePK=" + traveltimePK + " ]";
    }
    
}
