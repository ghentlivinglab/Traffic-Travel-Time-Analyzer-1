/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import domain.Trafficdata;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import simpledomain.IntervalTrafficData;
import simpledomain.LiveTrafficdata;
import simpledomain.SimpleTrafficdata;
import simpledomain.WeekdayTrafficdata;

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
    @Path("/live")
    @Produces({MediaType.APPLICATION_JSON})
    public String processLv(
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("15") @QueryParam("interval") Integer interval,
            @DefaultValue("30") @QueryParam("period") Integer period) {
        try {
            return "{\"result\":\"success\",\"data\":" + processLive(providerID, routeID, interval, period) + "}";
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    @GET
    @Path("/weekday")
    @Produces({MediaType.APPLICATION_JSON})
    public String processWkday(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("15") @QueryParam("interval") Integer interval,
            @QueryParam("weekday") Integer weekday) {
        if (from == null) from = new Timestamp(0);
        if (to == null) to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            return "{\"result\":\"success\",\"data\":" + processWeekday(from, to, routeID, providerID, interval, weekday) + "}";
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }
    
    @GET
    @Path("/interval")
    @Produces({MediaType.APPLICATION_JSON})
    public String processIntrvl(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("30") @QueryParam("slowSpeed") Integer slowSpeed) {
        if (from == null) from = new Timestamp(0);
        if (to == null) to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        try {
            return "{\"result\":\"success\",\"data\":" + processInterval(from, to, providerID, slowSpeed) + "}";
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    @GET
    //Used to be mode = default
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("15") @QueryParam("interval") Integer interval) {
        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }

        try {
            return "{\"result\":\"success\",\"data\":" + processDefault(from, to, routeID, providerID, interval) + "}";
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

    private String processDefault(Timestamp from, Timestamp to, Integer routeID, Integer providerID, Integer interval) throws Exception {
        if (routeID == null) {
            return processError(MessageState.RIDNP);
        }
        if (providerID == null) {
            return processError(MessageState.PIDNP);
        }

        String queryString = "select timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute, avg(traveltime) from trafficdata where timestamp between ?2 and ?3 and providerID=?4 and routeID=?5 group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%?1 minute order by 1";
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, interval);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);
        q.setParameter(4, providerID);
        q.setParameter(5, routeID);

        // Formaat is met die T ertussen! ' en ' zorgen ervoor dat hij dit letterlijk overneemt
        // -> dit is javascript formaat
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 

        
        StringBuilder json = new StringBuilder();
        try {
            json.append('{');
            String delimiter = "";
            for (Object[] o : (List<Object[]>) q.getResultList()) {
                json.append(delimiter).append(new SimpleTrafficdata((dateformat.format((Timestamp) o[0])), ((BigDecimal) o[1]).doubleValue()).toJson());
                delimiter = ",";
            }
            json.append('}');
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return json.toString();
    }

    private String processWeekday(Timestamp from, Timestamp to, Integer routeID, Integer providerID, Integer interval, Integer weekday) throws Exception {
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
        queryString += "group by WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i')";

        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, interval);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);
        q.setParameter(4, providerID);
        q.setParameter(5, routeID);
        q.setParameter(6, weekday);

        StringBuilder json = new StringBuilder();
        try {
            ArrayList<WeekdayTrafficdata> lijst = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                lijst.add(new WeekdayTrafficdata(i));
            }
            for (Object[] o : (List<Object[]>) q.getResultList()) {
                ((WeekdayTrafficdata) lijst.get(Integer.parseInt(o[0].toString()))).put((String) o[1], ((BigDecimal) o[2]).doubleValue());
            }
            json.append('{');
            String delimiter = "";
            for (WeekdayTrafficdata w : lijst) {
                json.append(delimiter).append(w.toJson());
                delimiter = ",";
            }
            json.append('}');
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return json.toString();
    }

    private String processLive(Integer providerID, Integer routeID, Integer interval, Integer period) throws Exception {
        if (providerID == null) {
            throw new Exception(MessageState.PIDNP);
        }
        String queryString = "SELECT x.routeID,max(x.timestamp),y.length, round((SELECT avg(traveltime) FROM   trafficdata  WHERE  providerid = x.providerid and routeid = x.routeid and timestamp = max(x.timestamp))) ,Round((SELECT Avg(traveltime)  FROM   trafficdata WHERE  providerid = x.providerid AND routeid = x.routeid AND timestamp > max(x.timestamp) - INTERVAL ?1 day  AND Abs(Minute(Timediff(Time(timestamp), Time(max(x.timestamp))))) < ?2 AND Weekday(timestamp) = Weekday(max(x.timestamp))), 0) from trafficdata x JOIN routes y ON x.routeid = y.id where x.providerID = ?3";
        //String queryString = "select x.routeID, x.timestamp, y.length, x.traveltime, round((select   avg(traveltime) from     trafficdata where    providerID=x.providerID and routeID=x.routeID and timestamp > now() - interval ?1 day and Abs(Minute(Timediff(Time(timestamp), Time(x.timestamp)))) < ?2 and weekday(timestamp) = weekday(x.timestamp) ),0) from trafficdata x join routes y on x.routeID=y.id where x.providerID=?3 ";
        if(routeID != null){
            queryString += " and routeID=?4 ";
        }
        queryString += " group by x.routeID;";
        System.out.println(queryString);
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, period);
        q.setParameter(2, interval);
        q.setParameter(3, providerID);
        if(routeID != null){
            q.setParameter(4, routeID);
        }
        
        StringBuilder json = new StringBuilder();
         ArrayList<LiveTrafficdata> lijst = new ArrayList<>();
        try{
            List<Object[]> rl = q.getResultList();
            if(rl != null){
            for (Object[] o : rl) {
                System.out.println(o[2].toString());
                LiveTrafficdata l = new LiveTrafficdata(Integer.parseInt(o[0].toString()));
                
                l.live.put("createdOn", o[1].toString());
                l.live.put("speed", "" + Math.round(Integer.parseInt(o[2].toString()) / Integer.parseInt(o[3].toString()) * 3.6 * 10.0) / 10.0);
                l.live.put("time", "" + Double.parseDouble(o[3].toString()) / 60 );
                l.avg.put("speed", "" + Math.round(Integer.parseInt(o[2].toString()) / Integer.parseInt(o[4].toString()) * 3.6 * 10.0) / 10.0);
                l.avg.put("time", "" + Double.parseDouble(o[4].toString()) / 60 );
                lijst.add(l);
            }
            }
        } catch (Exception e) {
            throw new Exception(e.getClass().getName() + "deel 1");
        }
        
        json.append('{');
        String delimiter = "";
        for (LiveTrafficdata l : lijst) {
            json.append(delimiter).append(l.toJson());
            delimiter = ",";
        }
        json.append('}');
        return json.toString();
    }
    
    private String processInterval(Timestamp from, Timestamp to,Integer providerID, Integer slowSpeed) throws Exception {
        if (providerID == null) {
            throw new Exception(MessageState.PIDNP);
        }
        String queryString = "SELECT routeid, Round(length / Avg(gem) * 3.6), Round(Avg(gem)), Sum(CASE WHEN weekday = 0 THEN slow_traffic END) AS ma, Sum(CASE WHEN weekday = 1 THEN slow_traffic END) AS di, Sum(CASE WHEN weekday = 2 THEN slow_traffic END) AS wo, Sum(CASE WHEN weekday = 3 THEN slow_traffic END) AS do, Sum(CASE WHEN weekday = 4 THEN slow_traffic END) AS vr, Sum(CASE WHEN weekday = 5 THEN slow_traffic END) AS za, Sum(CASE WHEN weekday = 6 THEN slow_traffic END) AS zo, Sum(CASE WHEN weekday is null THEN slow_traffic END) AS alles FROM (SELECT trafficdata.routeid, routes.length, Weekday(timestamp) AS weekday, Avg(trafficdata.traveltime) AS gem, Round(Count(CASE WHEN routes.length / trafficdata.traveltime * 3.6 < ?4 THEN 1 ELSE NULL END) / Count(*) * 100) AS slow_traffic FROM   trafficdata JOIN routes ON trafficdata.routeid = routes.id WHERE  providerid = ?3 AND trafficdata.timestamp BETWEEN ?1 AND ?2 GROUP  BY trafficdata.routeid, routes.length, Weekday(timestamp) WITH rollup HAVING trafficdata.routeid IS NOT NULL AND routes.length IS NOT NULL) x GROUP  BY routeid, length ";
        System.out.println(queryString);
        Query q = getEntityManager().createNativeQuery(queryString);
        
        q.setParameter(1, from, TemporalType.TIMESTAMP);
        q.setParameter(2, to, TemporalType.TIMESTAMP);
        q.setParameter(3, providerID);
        q.setParameter(4, slowSpeed);

        
        StringBuilder json = new StringBuilder();
         ArrayList<IntervalTrafficData> lijst = new ArrayList<>();
        try{
            List<Object[]> rl = q.getResultList();
            if(rl != null){
                for (Object[] o : rl) {
                    IntervalTrafficData l = new IntervalTrafficData(Integer.parseInt(o[0].toString()), Integer.parseInt(o[1].toString()), Integer.parseInt(o[2].toString()));
                    
                    for(int i = 0; i<8; i++){
                        int a = i+3;
                        if (o[a] == null){
                           l.weekdays[i] = 0;
                        }else{
                            System.out.println(o[a]);
                           l.weekdays[i] = Integer.parseInt(o[a].toString()); 
                        }
                    }

                    lijst.add(l);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getClass().getName() + "deel 1");
        }
        
        json.append('{');
        String delimiter = "";
        for (IntervalTrafficData l : lijst) {
            json.append(delimiter).append(l.toJson());
            delimiter = ",";
        }
        json.append('}');
        return json.toString();
    }
    
}
