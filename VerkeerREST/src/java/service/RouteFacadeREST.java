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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Contains the methods which handle HTTP-requests 
 * for path '/api/routes'.
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

    /**
     * Processes GET HTTP-requests for path '/api/routes/{routeID}'. {routeID}
     * must be an Integer value.
     *
     * @param routeID id of the route you want to retrieve
     * @return a single route, with the specified id, in JSON-format.
     */
    @GET
    @Path("/{routeID}")
    @Produces({MediaType.APPLICATION_JSON})
    public String processId(@PathParam("routeID") Integer routeID) {
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_RE_ID"));
        q.setParameter("routeID", routeID);
        List<Route> result = q.getResultList();
        if (!result.isEmpty()) {
            Route r = result.get(0);
            return processSuccess(routeToJSON(r));
        } else {
            return processError("No route found with id " + routeID + ".");
        }
    }

    /**
     * Processes GET HTTP-requests for path '/api/routes'.
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param routeID id of the route you want to retrieve
     * @param routeName name of the route you want to retrieve
     * @return a single route or a list of routes in JSON-format.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(@QueryParam("id") Integer routeID, @QueryParam("name") String routeName) {
        if (routeID != null) {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_RE_ID"));
            q.setParameter("routeID", routeID);
            List<Route> result = q.getResultList();
            if (!result.isEmpty()) {
                Route r = result.get(0);
                return processSuccess(routeToJSON(r));
            } else {
                return processError("No route found with id " + routeID + ".");
            }
        }
        if (routeName != null) {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_RE_NAME"));
            q.setParameter("name", routeName);
            List<Route> result = q.getResultList();
            if (!result.isEmpty()) {
                Route r = result.get(0);
                return processSuccess(routeToJSON(r));
            } else {
                return processError("No route found with name " + routeName + ".");
            }
        } else {
            List<Route> routes = super.findAll();
            return processSuccess(routeListToJSON(routes));
        }
    }

    /**
     * parses a route to a String in JSON-format.
     *
     * @param r the route to be parsed
     * @return String in JSON-format.
     */
    public String routeToJSON(Route r) {
        String delimiter = ",";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"description\":\"").append(r.getDescription()).append('"');
        sb.append(delimiter).append("\"endlat\":").append(r.getEndlat());
        sb.append(delimiter).append("\"endlong\":").append(r.getEndlong());
        sb.append(delimiter).append("\"id\":").append(r.getId());
        sb.append(delimiter).append("\"length\":").append(r.getLength());
        sb.append(delimiter).append("\"name\":\"").append(r.getName()).append('"');
        sb.append(delimiter).append("\"startlat\":").append(r.getStartlat());
        sb.append(delimiter).append("\"startlong\":").append(r.getStartlong());
        sb.append(delimiter).append("\"speedLimit\":").append(r.getSpeedLimit());
        sb.append("}");
        return sb.toString();
    }

    /**
     * parses a list of routes to a String in JSON-format.
     *
     * @param routes the list of routes to be parsed
     * @return String in JSON-format.
     */
    public String routeListToJSON(List<Route> routes) {
        String delimiter = "";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Route r : routes) {
            sb.append(delimiter).append(routeToJSON(r));
            delimiter = ",";
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
