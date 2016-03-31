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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        if (from == null) from = new Timestamp(0);
        if (to == null) to = new Timestamp(Calendar.getInstance().getTimeInMillis());
            
        try {
            switch(mode){
                case "default": return "{\"result\":\"succes\",\"data\":"+processDefault(from, to, routeID, providerID, interval)+"}";
                case "weekday": return "{\"result\":\"succes\",\"data\":"+processWeekday(from, to, routeID, providerID, interval, weekday)+"}";
                default: return processError(MessageState.MDNE);
            }
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    private String processError(String message) {
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

    private String processDefault(Timestamp from, Timestamp to, Integer routeID, Integer providerID, Integer interval) throws Exception{
        if (routeID == null) return processError(MessageState.RIDNP);
        if (providerID == null) return processError(MessageState.PIDNP);
        
        String queryString = "select timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute, avg(traveltime) from trafficdata where timestamp between ?2 and ?3 and providerID=?4 and routeID=?5 group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute order by 1";
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, interval);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);
        q.setParameter(4, providerID);
        q.setParameter(5, routeID);
        
        StringBuilder json = new StringBuilder();
        try {
            json.append('{');
            String delimiter = "";
            for (Object[] o : (List<Object[]>) q.getResultList()) {
                json.append(delimiter).append(new SimpleTrafficdata(((Timestamp) o[0]).toString(), ((BigDecimal) o[1]).doubleValue()).toJson());
                delimiter = ",";
            }
            json.append('}');
        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
        
        return json.toString();
    }
    private String processWeekday(Timestamp from, Timestamp to, Integer routeID, Integer providerID, Integer interval, Integer weekday) throws Exception{
        String queryString = "SELECT WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i'), AVG(traveltime) FROM trafficdata WHERE timestamp between ?2 and ?3 ";
        if (providerID != null) queryString += " and providerID=?4 ";
        if (routeID != null) queryString += " and routeID=?5 ";
        if (weekday != null) queryString += " and WEEKDAY(TIMESTAMP)=?6 ";
        queryString += "group by WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i')";
        
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, interval);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);
        q.setParameter(4, providerID);
        q.setParameter(5, routeID);
        q.setParameter(6, weekday);
        
        StringBuilder json = new StringBuilder();
        List<Object[]> objects;
        try {
            ArrayList<WeekdayTrafficdata> lijst = new ArrayList<>();
            for (int i = 0; i < 7; i++) lijst.add(new WeekdayTrafficdata(i));
            objects = q.getResultList();
            Genson g = new Genson();
            System.out.println(g.serialize(objects)); 
            for (Object[] o : objects) { 
                WeekdayTrafficdata w = (WeekdayTrafficdata)lijst.get(Integer.parseInt(o[0].toString()));
                w.put((String) o[1], ((BigDecimal) o[2]).doubleValue()); 
            }
            json.append('{');
            String delimiter = "";
            for (WeekdayTrafficdata w : lijst) {
                json.append(delimiter).append(w.toJson());
                delimiter = ",";
            }
            json.append('}');
            
        } catch(Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        
        return json.toString();
    }
    
}
