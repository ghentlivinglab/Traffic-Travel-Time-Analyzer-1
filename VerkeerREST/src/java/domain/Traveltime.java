/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Robin
 */
@Entity
@Table(name = "traveltimes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Traveltime.findAll", query = "SELECT t FROM Traveltime t"),
    @NamedQuery(name = "Traveltime.findById", query = "SELECT t FROM Traveltime t WHERE t.id = :id"),
    @NamedQuery(name = "Traveltime.findByTraveltime", query = "SELECT t FROM Traveltime t WHERE t.traveltime = :traveltime")})
public class Traveltime implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "traveltime")
    private Double traveltime;
    @JoinColumn(name = "providerID", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Provider providerID;
    @JoinColumn(name = "routeID", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Route routeID;

    public Traveltime() {
    }

    public Traveltime(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTraveltime() {
        return traveltime;
    }

    public void setTraveltime(Double traveltime) {
        this.traveltime = traveltime;
    }

    public Provider getProviderID() {
        return providerID;
    }

    public void setProviderID(Provider providerID) {
        this.providerID = providerID;
    }

    public Route getRouteID() {
        return routeID;
    }

    public void setRouteID(Route routeID) {
        this.routeID = routeID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Traveltime)) {
            return false;
        }
        Traveltime other = (Traveltime) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Traveltime[ id=" + id + " ]";
    }
    
}
