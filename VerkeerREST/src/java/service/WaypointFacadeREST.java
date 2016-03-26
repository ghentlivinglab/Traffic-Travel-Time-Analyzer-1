/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import domain.Waypoint;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Robin
 */
@Stateless
@Path("waypoints")
public class WaypointFacadeREST extends AbstractFacade<Waypoint> {

    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public WaypointFacadeREST() {
        super(Waypoint.class);
    }

    

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Waypoint> findAll() {
        return super.findAll();
    }

    

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
