/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys.service;

import entitys.Route;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author Piet
 */
@Stateless
@Path("route")
public class RouteFacadeREST extends AbstractFacade<Route> {
    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public RouteFacadeREST() {
        super(Route.class);
    }

    @POST
    @Override
    @Consumes({"application/json"})
    public void create(Route entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/json"})
    public void edit(@PathParam("id") Integer id, Route entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id}")
    @Produces({"application/json"})
    public Route find(@PathParam("id") Integer id) {
        return super.find(id);
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("id={id}")
    @Produces({"application/json"})
    public Route findById(@PathParam("id") Integer id) {
        return super.find(id);
    }
    
    /**
     * 
     * @param routeNaam
     * @return 
     */
    @GET
    @Path("naam={routeNaam}")
    @Produces({"application/json"})
    public Route findByName(@PathParam("routeNaam") String routeNaam) {
        return super.findByName(routeNaam);
    }
    
    @GET
    @Override
    @Produces({"application/json"})
    public List<Route> findAll() {
        return super.findAll();
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
