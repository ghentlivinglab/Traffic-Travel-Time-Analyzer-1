/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

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
 * @author Robin
 */
@Entity
@Table(name = "providers")
@XmlRootElement
public class Provider implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 50)
    @Column(name = "name")
    private String name;
    @Column(name = "weight")
    private Integer weight;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "providerID")
    private Collection<Trafficdata> trafficdataCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "providerID")
    private Collection<Traveltime> traveltimeCollection;

    public Provider() {
    }

    public Provider(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
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
        if (!(object instanceof Provider)) {
            return false;
        }
        Provider other = (Provider) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "domain.Provider[ id=" + id + " ]";
    }
    
}
