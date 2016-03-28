/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.owlike.genson.Genson;
import simpledomain.SimpleTrafficdata;
import simpledomain.WeekdayTrafficdata;
import domain.Trafficdata;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.ws.rs.DefaultValue;
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
@Path("/trafficdata")
public class TrafficdataFacadeREST extends AbstractFacade<Trafficdata> {

    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public TrafficdataFacadeREST() {
        super(Trafficdata.class);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("60") @QueryParam("interval") Integer interval,
            @DefaultValue("default") @QueryParam("mode") String mode,
            @QueryParam("weekday") Integer weekday) {
        String json = "";
        Genson g = new Genson();

        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }

        Query q = getQuery(mode, routeID, providerID, from, to, interval, weekday);
        List<Object[]> objects = (List<Object[]>) q.getResultList();
        if (mode.equals("default")) {
            ArrayList<SimpleTrafficdata> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                lijst.add(new SimpleTrafficdata((Timestamp) o[0], ((BigDecimal) o[1]).doubleValue()));
            }
            json = g.serialize(lijst);
        } 
        else if (mode.equals("weekday")) {
            ArrayList<WeekdayTrafficdata> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                System.out.println( o[0] + " " + ((BigDecimal) o[1]).doubleValue());
                if(weekday != null){
                    lijst.add(new WeekdayTrafficdata((int)(long) o[0], ((BigDecimal) o[1]).doubleValue()));
                }
                else{
                    lijst.add(new WeekdayTrafficdata((int) o[0], ((BigDecimal) o[1]).doubleValue()));
                }
            }

            json = g.serialize(lijst);
        }
        return json;
    }

    public Query getQuery(String mode, Integer routeID, Integer providerID, Timestamp from, Timestamp to, Integer interval, Integer weekday) {
        Query q = null;
        if (mode.equals("default")) {
            String queryString = "select timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute, avg(traveltime) from trafficdata where timestamp between ?2 and ?3 ";
            if (providerID != null) {
                queryString += " and providerID=?4 ";
            }
            if (routeID != null) {
                queryString += " and routeID=?5 ";
            }
            queryString += " group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute";

            q = getEntityManager().createNativeQuery(queryString);
            if (providerID != null) {
                q.setParameter(4, providerID);
            }
            if (routeID != null) {
                q.setParameter(5, routeID);
            }
            q.setParameter(2, from, TemporalType.TIMESTAMP);
            q.setParameter(3, to, TemporalType.TIMESTAMP);
            q.setParameter(1, interval);
        } else if (mode.equals("weekday")) {

            String queryString = "SELECT WEEKDAY(TIMESTAMP), AVG(traveltime) FROM trafficdata WHERE timestamp between ?1 and ?2 ";
            if (providerID != null) {
                queryString += " and providerID=?3 ";
            }
            if (routeID != null) {
                queryString += " and routeID=?4 ";
            }
            if (weekday != null) {
                queryString += " and WEEKDAY(TIMESTAMP)=?5 ";
            }
            queryString += " GROUP BY WEEKDAY(TIMESTAMP)";
            q = getEntityManager().createNativeQuery(queryString);
            if (providerID != null) {
                q.setParameter(3, providerID);
            }
            if (routeID != null) {
                q.setParameter(4, routeID);
            }
            if (weekday != null) {
                q.setParameter(5, weekday);
            }
            q.setParameter(1, from, TemporalType.TIMESTAMP);
            q.setParameter(2, to, TemporalType.TIMESTAMP);

        }
        return q;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
