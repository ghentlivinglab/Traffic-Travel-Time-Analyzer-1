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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "waypoints")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Waypoint.findAll", query = "SELECT w FROM Waypoint w"),
    @NamedQuery(name = "Waypoint.findById", query = "SELECT w FROM Waypoint w WHERE w.id = :id"),
    @NamedQuery(name = "Waypoint.findBySequence", query = "SELECT w FROM Waypoint w WHERE w.sequence = :sequence"),
    @NamedQuery(name = "Waypoint.findByLatitude", query = "SELECT w FROM Waypoint w WHERE w.latitude = :latitude"),
    @NamedQuery(name = "Waypoint.findByLongitude", query = "SELECT w FROM Waypoint w WHERE w.longitude = :longitude")})
public class Waypoint implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sequence")
    private int sequence;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @JoinColumn(name = "routeID", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Route routeID;

    public Waypoint() {
    }

    public Waypoint(Integer id) {
        this.id = id;
    }

    public Waypoint(Integer id, int sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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
        if (!(object instanceof Waypoint)) {
            return false;
        }
        Waypoint other = (Waypoint) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Waypoint[ id=" + id + " ]";
    }
    
}
