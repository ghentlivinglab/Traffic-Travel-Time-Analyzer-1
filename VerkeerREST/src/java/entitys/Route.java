/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Piet
 */
@Entity
@Table(name = "routes")
@XmlRootElement
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "length")
    private Double length;
    @Size(max = 50)
    @Column(name = "name")
    private String name;
    @Column(name = "startlat")
    private Double startlat;
    @Column(name = "startlong")
    private Double startlong;
    @Column(name = "endlat")
    private Double endlat;
    @Column(name = "endlong")
    private Double endlong;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "route")
    private Collection<Trafficdata> trafficdataCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "route")
    private Collection<Traveltime> traveltimeCollection;

    public Route() {
    }

    public Route(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getStartlat() {
        return startlat;
    }

    public void setStartlat(Double startlat) {
        this.startlat = startlat;
    }

    public Double getStartlong() {
        return startlong;
    }

    public void setStartlong(Double startlong) {
        this.startlong = startlong;
    }

    public Double getEndlat() {
        return endlat;
    }

    public void setEndlat(Double endlat) {
        this.endlat = endlat;
    }

    public Double getEndlong() {
        return endlong;
    }

    public void setEndlong(Double endlong) {
        this.endlong = endlong;
    }

    @XmlTransient
    public Collection<Trafficdata> getTrafficdataCollection() {
        return trafficdataCollection;
    }

    public void setTrafficdataCollection(Collection<Trafficdata> trafficdataCollection) {
        this.trafficdataCollection = trafficdataCollection;
    }

    @XmlTransient
    public Collection<Traveltime> getTraveltimeCollection() {
        return traveltimeCollection;
    }

    public void setTraveltimeCollection(Collection<Traveltime> traveltimeCollection) {
        this.traveltimeCollection = traveltimeCollection;
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
        if (!(object instanceof Route)) {
            return false;
        }
        Route other = (Route) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entitys.Route[ id=" + id + " ]";
    }
    
}
