/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.owlike.genson.Genson;
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
@Path("trafficdata")
public class TrafficdataFacadeREST extends AbstractFacade<Trafficdata> {

    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public TrafficdataFacadeREST() {
        super(Trafficdata.class);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String findAll(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @QueryParam("interval") Integer interval,
            @QueryParam("mode") String mode,
            @QueryParam("weekday") Integer weekday) {
        String json = "";
        Genson g = new Genson();
        boolean isSetRouteID = false;
        boolean isSetProviderID = false;
        if (mode == null) {
            mode = "default";
        }
        if (interval == null) {
            interval = 60;
        }
        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }

        if (mode.equals("default")) {
            /* Default mode (live):
               {
                    from-->
                    ...
                    "6:00": 689, per interval
                    "6:15": 126;
                    ...
                    <--to
                }
                Vanaf 'from' tot 'to' worden voor de meegegeven routeID en providerID alle data per interval opgehaald.
                Hierbij ga ik een klasse SimpleTrafficdata gebruiken.
                In deze mode wordt de QueryParam weekday genegeerd.
             */
            class SimpleTrafficdata implements Serializable {

                double traveltime;
                Timestamp timestamp;

                public SimpleTrafficdata(Timestamp ts, double tr) {
                    traveltime = tr;
                    timestamp = ts;
                }
            }

            String query = "select timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, avg(traveltime) from trafficdata where timestamp between ?2 and ?3 ";
            if (providerID != null && routeID != null) {
                query += " and providerID=?4 and routeID=?5 ";
                isSetProviderID = true;
                isSetRouteID = true;
            } else if (providerID != null && routeID == null) {
                query += " and providerID=?4 ";
                isSetProviderID = true;
            } else if (providerID == null && routeID != null) {
                query += " and routeID=?4 ";
                isSetRouteID = true;
            }
            query += " group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute";

            Query q = getEntityManager().createNativeQuery(query);
            if (isSetProviderID && isSetRouteID) {
                q.setParameter(4, providerID);
                q.setParameter(5, routeID);
            } else if (isSetProviderID) {
                q.setParameter(4, providerID);
            } else if (isSetRouteID) {
                q.setParameter(4, routeID);
            }
            q.setParameter(2, from, TemporalType.TIMESTAMP);
            q.setParameter(3, to, TemporalType.TIMESTAMP);
            q.setParameter(1, interval);

            List<Object[]> objects = (List<Object[]>) q.getResultList();

            ArrayList<SimpleTrafficdata> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                lijst.add(new SimpleTrafficdata((Timestamp) o[0], ((BigDecimal) o[1]).doubleValue()));
            }
            json = g.serialize(lijst);

        } else if (mode.equals("weekday")) {
            /*  Weekday mode:
                { 
                    0: 123,         maandag
                    1: 548,         dinsdag
                    ...
                    6: 999,         zondag
                }
                voor alle dagen van 'from' tot 'to' wordt er per weekdag een gemiddelde genomen om periodes te kunnen bekijken.
                de optie weekday specifieerd een bepaalde dag zodat voor weekday=5 deze eenvoudige output wordt gegenereerd (in den aard):
                {
                    5: 123
                }
             */

            class WeekdayTrafficdata implements Serializable {

                int weekday;
                double traveltime;

                public WeekdayTrafficdata(int weekday, double traveltime) {
                    this.weekday = weekday;
                    this.traveltime = traveltime;
                }
            }
            String query = "SELECT WEEKDAY(TIMESTAMP), AVG(traveltime) FROM trafficdata WHERE timestamp between ?1 and ?2 ";
            if (providerID != null && routeID != null) {
                query += " and providerID=?3 and routeID=?4 ";
                isSetProviderID = true;
                isSetRouteID = true;
            } else if (providerID != null && routeID == null) {
                query += " and providerID=?3 ";
                isSetProviderID = true;
            } else if (providerID == null && routeID != null) {
                query += " and routeID=?3 ";
                isSetRouteID = true;
            }
            query += " GROUP BY WEEKDAY(TIMESTAMP)";
            Query q = getEntityManager().createNativeQuery(query);
            if (isSetProviderID && isSetRouteID) {
                q.setParameter(3, providerID);
                q.setParameter(4, routeID);
            } else if (isSetProviderID) {
                q.setParameter(3, providerID);
            } else if (isSetRouteID) {
                q.setParameter(3, routeID);
            }
            q.setParameter(1, from, TemporalType.TIMESTAMP);
            q.setParameter(2, to, TemporalType.TIMESTAMP);

            List<Object[]> objects = (List<Object[]>) q.getResultList();

            ArrayList<WeekdayTrafficdata> lijst = new ArrayList<>();
            for (Object[] o : objects) {
                lijst.add(new WeekdayTrafficdata((int) o[0], ((BigDecimal) o[1]).doubleValue()));
            }

            json = g.serialize(lijst);
        }
        return json;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
