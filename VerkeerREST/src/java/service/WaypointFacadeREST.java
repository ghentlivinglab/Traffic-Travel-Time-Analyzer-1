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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import simpledomain.SimpleWaypoint;

/**
 * Contains the methods which handle HTTP-requests 
 * for path '/api/waypoints'.
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

    /**
     * Processes GET HTTP-requests for path '/api/waypoints/{routeID}
     * {routeID} must be an Integer value
     * 
     * @param routeID id of the route you want to retrieve the waypoints for
     * @return a list of all the waypoints of the route with the specified id
     */
    @GET
    @Path("{routeID}")
    @Produces({MediaType.APPLICATION_JSON})
    public String processId(@PathParam("routeID") Integer routeID) {
        Genson g = new Genson();
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_WE_ID"));
        q.setParameter("routeID", routeID);
        List<Object[]> objects = (List<Object[]>) q.getResultList();
        if (!objects.isEmpty()) {
            ArrayList<SimpleWaypoint> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                System.out.println((int) o[0] + " " + (int) o[1] + " latitude : " + (double) o[2] + " longitude : " + (double) o[3]);
                lijst.add(new SimpleWaypoint((int) o[0], (int) o[1], (double) o[2], (double) o[3]));
            }
            return processSuccess(g.serialize(lijst));
        } else {
            return processError("No waypoints found for route with id " + routeID + ".");
        }
    }
    
    /**
     * Processes GET HTTP-requests for path '/api/waypoints'.
     * <p>
     * parameters must be separated from the path by a '?' sign,
     * multiple parameters must be separated from each other by an '&' sign.
     * 
     * @param routeID id of the route you want to retrieve the waypoints for
     * @return a list of all the waypoints of all the waypoints for all the providers
     * or a list of all the waypoints for the specified route
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(@QueryParam("routeID") Integer routeID) {
        Genson g = new Genson();
        if (routeID != null) {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_WE_ID"));
            q.setParameter("routeID", routeID);
            List<Object[]> objects = (List<Object[]>) q.getResultList();
            if (!objects.isEmpty()) {
                ArrayList<SimpleWaypoint> lijst = new ArrayList<>();
                for (Object[] o : objects) {
                    System.out.println((int) o[0] + " " + (int) o[1] + " latitude : " + (double) o[2] + " longitude : " + (double) o[3]);
                    lijst.add(new SimpleWaypoint((int) o[0], (int) o[1], (double) o[2], (double) o[3]));
                }
                return processSuccess(g.serialize(lijst));
            } else {
                return processError("No waypoints found for route with id " + routeID + ".");
            }
        } else {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_WE"));
            List<Object[]> objects = (List<Object[]>) q.getResultList();
            ArrayList<SimpleWaypoint> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                System.out.println("latitude : " + (double) o[2] + " longitude : " + (double) o[3]);
                lijst.add(new SimpleWaypoint((int) o[0], (int) o[1], (double) o[2], (double) o[3]));
            }
            return processSuccess(g.serialize(lijst));
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
