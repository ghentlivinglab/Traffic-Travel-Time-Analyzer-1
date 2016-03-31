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
import java.util.Arrays;
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

    private List<String> modes;
    
    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    public TrafficdataFacadeREST() {
        super(Trafficdata.class);
        modes = new ArrayList<>(Arrays.asList("default","weekday","live"));
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
        StringBuilder json = new StringBuilder();
        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }

        //Filter dat slechte requests tegenhoud. 
        if(!modes.contains(mode)) return constructErrorJson(MessageState.MDNE);
        if (mode.equals("default")) {
            if (routeID == null) {
                return constructErrorJson(MessageState.RIDNP);
            } else if (providerID == null) {
                return constructErrorJson(MessageState.PIDNP);
            }
        }

        // Als je hier geraakt wil dat zeggen dat het request geldig is. Tenzij de inhoud van de parameters ongeldig is. 
        // In dat geval zal wss een exceptie opgegooid worden en dat fout bericht wordt dan meegestuurd in de json.
        Query q = getQuery(mode, routeID, providerID, from, to, interval, weekday);
        List<Object[]> objects;
        try {
            objects = (List<Object[]>) q.getResultList();

            json.append("{\"result\":\"succes\",\"data\":");
            if (mode.equals("default")) {
                json.append('{');
                String delimiter = "";
                for (Object[] o : objects) {
                    json.append(delimiter).append(new SimpleTrafficdata(((Timestamp) o[0]).toString(), ((BigDecimal) o[1]).doubleValue()).toJson());
                    delimiter = ",";
                }
                json.append('}');
            } else if (mode.equals("weekday")) {
                ArrayList<WeekdayTrafficdata> lijst = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    lijst.add(new WeekdayTrafficdata(i));
                }
                for (Object[] o : objects) {
                    lijst.get((int) o[0]).put((String) o[1], ((BigDecimal) o[2]).doubleValue());
                }
                json.append('{');
                String delimiter = "";
                for (WeekdayTrafficdata w : lijst) {
                    json.append(delimiter).append(w.toJson());
                    delimiter = ",";
                }
                json.append('}');
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return constructErrorJson(e.getMessage());
        }
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
            queryString += " group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute order by 1";

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

            String queryString = "SELECT WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i'), AVG(traveltime) FROM trafficdata WHERE timestamp between ?2 and ?3 ";
            if (providerID != null) {
                queryString += " and providerID=?4 ";
            }
            if (routeID != null) {
                queryString += " and routeID=?5 ";
            }
            if (weekday != null) {
                queryString += " and WEEKDAY(TIMESTAMP)=?6 ";
            }
            System.out.println(queryString);
            queryString += " GROUP BY WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i')";
            q = getEntityManager().createNativeQuery(queryString);
            if (providerID != null) {
                q.setParameter(4, providerID);
            }
            if (routeID != null) {
                q.setParameter(5, routeID);
            }
            if (weekday != null) {
                q.setParameter(6, weekday);
            }
            q.setParameter(1, interval);
            q.setParameter(2, from, TemporalType.TIMESTAMP);
            q.setParameter(3, to, TemporalType.TIMESTAMP);

        }
        return q;
    }

    private String constructErrorJson(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"result\": ").append("\"error\",");
        sb.append("\"reason\": \"").append(message).append('"');
        sb.append('}');
        return sb.toString();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
