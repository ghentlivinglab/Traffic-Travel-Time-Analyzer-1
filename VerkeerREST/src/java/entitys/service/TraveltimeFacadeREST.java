/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys.service;

import entitys.Traveltime;
import entitys.TraveltimePK;
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
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Piet
 */
@Stateless
@Path("traveltime")
public class TraveltimeFacadeREST extends AbstractFacade<Traveltime> {
    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    private TraveltimePK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;routeID=routeIDValue;providerID=providerIDValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        entitys.TraveltimePK key = new entitys.TraveltimePK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> routeID = map.get("routeID");
        if (routeID != null && !routeID.isEmpty()) {
            key.setRouteID(new java.lang.Integer(routeID.get(0)));
        }
        java.util.List<String> providerID = map.get("providerID");
        if (providerID != null && !providerID.isEmpty()) {
            key.setProviderID(new java.lang.Integer(providerID.get(0)));
        }
        return key;
    }

    public TraveltimeFacadeREST() {
        super(Traveltime.class);
    }

    @POST
    @Override
    @Consumes({"application/json"})
    public void create(Traveltime entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/json"})
    public void edit(@PathParam("id") PathSegment id, Traveltime entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        entitys.TraveltimePK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({"application/json"})
    public Traveltime find(@PathParam("id") PathSegment id) {
        entitys.TraveltimePK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({"application/json"})
    public List<Traveltime> findAll() {
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
