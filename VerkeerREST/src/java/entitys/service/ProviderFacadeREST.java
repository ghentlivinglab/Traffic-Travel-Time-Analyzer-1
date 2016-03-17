/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys.service;

import entitys.Provider;
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
@Path("provider")
public class ProviderFacadeREST extends AbstractFacade<Provider> {
    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public ProviderFacadeREST() {
        super(Provider.class);
    }

    @POST
    @Override
    @Consumes({"application/json"})
    public void create(Provider entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/json"})
    public void edit(@PathParam("id") Integer id, Provider entity) {
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
    public Provider find(@PathParam("id") Integer id) {
        System.out.println("yep, provider gevonden");
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
    public Provider findById(@PathParam("id") Integer id) {
        return super.find(id);
    }
    
    /**
     * 
     * @param providerNaam
     * @return 
     */
    @GET
    @Path("naam={providerNaam}")
    @Produces({"application/json"})
    public Provider findByName(@PathParam("providerNaam") String providerNaam) {
        return super.findByName(providerNaam);
    }
    
    
    @GET
    @Override
    @Produces({"application/json"})
    public List<Provider> findAll() {
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
