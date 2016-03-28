/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.owlike.genson.Genson;
import domain.Waypoint;
import java.util.ArrayList;
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
import simpledomain.SimpleWaypoint;

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
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(@QueryParam("routeID") Integer routeID) {
        String json = "";
        Genson g = new Genson();
        if(routeID != null){
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_WE_ID"));
            q.setParameter("routeID", routeID);
            List<Object[]> objects = (List<Object[]>) q.getResultList();
        
            ArrayList<SimpleWaypoint> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                System.out.println((int) o[0] +" " +(int) o[1]+" "+(double) o[2]+" " +(double) o[3]);
                lijst.add(new SimpleWaypoint((int) o[0],(int) o[1],(double) o[2],(double) o[3]));
            }
            return g.serialize(lijst);
        }
        else{
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_WE"));
            List<Object[]> objects = (List<Object[]>) q.getResultList();
            ArrayList<SimpleWaypoint> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                lijst.add(new SimpleWaypoint((int) o[0],(int) o[1],(double) o[2],(double) o[3]));
            }
            return g.serialize(lijst);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
