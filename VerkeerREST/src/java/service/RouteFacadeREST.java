/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import domain.Route;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Robin
 */
@Stateless
@Path("/routes")
public class RouteFacadeREST extends AbstractFacade<Route> {

    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public RouteFacadeREST() {
        super(Route.class);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Route> processRequest(@QueryParam("id") Integer routeID, @QueryParam("name") String routeName) {
        if(routeID!= null){
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_RE_ID"));
            q.setParameter("routeID", routeID);
            return q.getResultList();
        }
        if(routeName != null){
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_RE_NAME"));
            q.setParameter("name", routeName);
            return q.getResultList();
        }
        else{
            return super.findAll();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
