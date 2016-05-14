/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import domain.Trafficdata;
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
 * Contains the methods which handle HTTP-requests for path '/api/trafficdata'.
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

    /**
     * Processes GET HTTP-requests for path '/api/trafficdata/live'.
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param routeID id of the route you want to retrieve data for
     * @param providerID id of the provider you want to retrieve data for
     * @param interval interval (in minutes) used to calculate the average data.
     * Thus the data {interval} minutes before and after the live-time is used.
     * Interval is default 15 minutes
     * @param period the period (in days) used to calculate the average data.
     * Thus the data {period} days before the live-time is used. Period is
     * default 30 days
     * @return Most recent data for specified route and provider. Data includes
     * createdOn, time and speed for live data and time and speed for average
     * data.
     */
    @GET
    @Path("/live")
    @Produces({MediaType.APPLICATION_JSON})
    public String processLv(
            @QueryParam("routeID") Integer routeID,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("15") @QueryParam("interval") Integer interval,
            @DefaultValue("30") @QueryParam("period") Integer period) {
        try {
            return processSuccess(processLive(providerID, routeID, interval, period));
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    /**
     * Processes GET HTTP-requests for path '/api/trafficdata/weekday'.
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param from Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the
     * start of the period you want the data for
     * @param to Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the end
     * of the period you want the data for
     * @param routeID id of the route you want to retrieve data for
     * @param providerID id of the provider you want to retrieve data for
     * @param interval interval (in minutes) between the data. Interval is
     * default 15 minutes
     * @param weekday the day you want to retrieve data for. (monday=0,
     * tuesday=1, ..., sunday=6)
     * @return data for every day (0..6) for specified route and provider. Data
     * includes Time {format HH:mm} and traveltime.
     */
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
        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }
        try {
            return processSuccess(processWeekday(from, to, routeID, providerID, interval, weekday));
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    /**
     * Processes GET HTTP-requests for path '/api/trafficdata/interval'.
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param from Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the
     * start of the period you want the data for
     * @param to Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the end
     * of the period you want the data for
     * @param providerID id of the provider you want to retrieve data for
     * @param slowSpeed
     * @return data for every day (0..6) for specified route and provider. Data
     * includes Time {format HH:mm} and traveltime.
     */
    @GET
    @Path("/interval")
    @Produces({MediaType.APPLICATION_JSON})
    public String processIntrvl(
            @QueryParam("from") Timestamp from,
            @QueryParam("to") Timestamp to,
            @QueryParam("providerID") Integer providerID,
            @DefaultValue("15") @QueryParam("slowSpeed") Integer slowSpeed) {
        if (from == null) {
            from = new Timestamp(0);
        }
        if (to == null) {
            to = new Timestamp(Calendar.getInstance().getTimeInMillis());
        }
        try {
            return processSuccess(processInterval(from, to, providerID, slowSpeed));
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
    }

    /**
     * Processes GET HTTP-requests for path '/api/trafficdata'.
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param from Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the
     * start of the period you want the data for
     * @param to Timestamp {format yyyy-MM-dd HH:mm:ss} which specifies the end
     * of the period you want the data for
     * @param routeID id of the route you want to retrieve data for
     * @param providerID id of the provider you want to retrieve data for
     * @param interval interval (in minutes) between the data. Interval is
     * default 15 minutes
     * @return a list of data for the specified period. Data includes Timestamp
     * {format yyyy-MM-dd HH:mm:ss} and traveltime.
     */
    @GET
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
            return processSuccess(processDefault(from, to, routeID, providerID, interval));
        } catch (Exception ex) {
            return processError(ex.getMessage());
        }
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

        String queryString = "SELECT From_unixtime(Floor(( Unix_timestamp(timestamp) + ?1 /2) /?1) * ?1), Avg(traveltime), Avg(avgtraveltimeday) FROM trafficdata WHERE timestamp BETWEEN ?2 AND ?3 AND providerid = ?4 AND routeid = ?5 AND avgtraveltimeday is not null and avgtraveltimeday != 0 GROUP BY From_unixtime(Floor(( Unix_timestamp(timestamp) + ?1 /2) /?1) * ?1) ORDER BY 1 ;";
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, interval*60);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);
        q.setParameter(4, providerID);
        q.setParameter(5, routeID);

        StringBuilder json = new StringBuilder();
        try {
            json.append('{');
            String delimiter = "";
            for (Object[] o : (List<Object[]>) q.getResultList()) {
                json.append(delimiter).append(new SimpleTrafficdata(o[0].toString(), ((BigDecimal) o[1]).doubleValue(), ((BigDecimal) o[2]).doubleValue()).toJson());
                delimiter = ",";
            }
            json.append('}');
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return json.toString();
    }

    private String processWeekday(Timestamp from, Timestamp to, Integer routeID, Integer providerID, Integer interval, Integer weekday) throws Exception {
        String queryString = "SELECT WEEKDAY(TIMESTAMP), DATE_FORMAT(STR_TO_DATE(timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)% ?1 minute, '%Y-%m-%d %H:%i:%s'), '%H:%i'), AVG(traveltime), AVG(avgtraveltimeday) FROM trafficdata WHERE timestamp between ?2 and ?3 ";
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
                // Haalt de weekday uit de lijst, en zet de juiste gegevens
                ((WeekdayTrafficdata) lijst.get(Integer.parseInt(o[0].toString()))).put((String) o[1], ((BigDecimal) o[2]).doubleValue(), ((BigDecimal) o[3]).doubleValue());
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
        String queryString = "SELECT x.routeid, Max(x.timestamp), y.length, Round((SELECT Avg(traveltime) FROM trafficdata WHERE providerid = x.providerid AND routeid = x.routeid AND timestamp = Max(x.timestamp))), Round((SELECT Avg(avgtraveltimeday) FROM trafficdata WHERE providerid = x.providerid AND routeid = x.routeid AND timestamp = Max(x.timestamp))) FROM trafficdata x JOIN routes y ON x.routeid = y.id WHERE x.providerid = ?1";
        //String queryString = "select x.routeID, x.timestamp, y.length, x.traveltime, round((select   avg(traveltime) from     trafficdata where    providerID=x.providerID and routeID=x.routeID and timestamp > now() - interval ?1 day and Abs(Minute(Timediff(Time(timestamp), Time(x.timestamp)))) < ?2 and weekday(timestamp) = weekday(x.timestamp) ),0) from trafficdata x join routes y on x.routeID=y.id where x.providerID=?3 ";
        if (routeID != null) {
            queryString += " and routeID=?2 ";
        }
        queryString += " and avgtraveltimeday != 0 ";
        queryString += " group by x.routeID, y.length;";
        
        //System.out.println(queryString);
        Query q = getEntityManager().createNativeQuery(queryString);
        q.setParameter(1, providerID);
        /*q.setParameter(2, interval);
        q.setParameter(3, providerID);*/
        if (routeID != null) {
            q.setParameter(2, routeID);
        }

        StringBuilder json = new StringBuilder();
        ArrayList<LiveTrafficdata> lijst = new ArrayList<>();
        try {
            List<Object[]> rl = q.getResultList();
            if (rl != null) {
                for (Object[] o : rl) {
                    //System.out.println(o[2].toString());
                    LiveTrafficdata l = new LiveTrafficdata(Integer.parseInt(o[0].toString()));

                    l.live.put("createdOn", o[1].toString());
                    l.live.put("speed", "" + Math.round(Integer.parseInt(o[2].toString()) / (double) Integer.parseInt(o[3].toString()) * 3.6 * 10.0) / 10.0);
                    l.live.put("time", "" + Double.parseDouble(o[3].toString()) / 60);
                    l.avg.put("speed", "" + Math.round(Integer.parseInt(o[2].toString()) / (double) Integer.parseInt(o[4].toString()) * 3.6 * 10.0) / 10.0);
                    l.avg.put("time", "" + Double.parseDouble(o[4].toString()) / 60);
                    lijst.add(l);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getClass().getName() + " "+e.getMessage());
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

    private String processInterval(Timestamp from, Timestamp to, Integer providerID, Integer slowSpeed) throws Exception {
        if (providerID == null) {
            throw new Exception(MessageState.PIDNP_I);
        }
        String queryString = "SELECT x.routeid, Round(Avg(routes.length / x.traveltime * 3.6)), Round(Avg(traveltime)), Ceil(Sum(x.difference)/432 + 50), Group_concat(DISTINCT CASE WHEN x.unusual_count > 0.7 THEN Concat('\\\"', x.timestamp, '\\\"') end ORDER BY unusual_count DESC) FROM(SELECT routeid, From_unixtime(Floor(( Unix_timestamp(timestamp) + 900) / 1800) * 1800) AS timestamp, Avg(traveltime) AS traveltime, Avg(avgtraveltimeday) AS avgtraveltimeday, Sum(avgtraveltimeday - traveltime) as difference, Count(CASE WHEN traveltime > avgtraveltimeday * 2 AND avgtraveltimeday != 0 THEN 1 end) / 5 AS unusual_count FROM trafficdata WHERE providerid = ?1 AND timestamp BETWEEN ?2 AND ?3 GROUP BY routeid, From_unixtime(Floor(( Unix_timestamp(timestamp) + 900 ) / 1800) * 1800)) x JOIN routes ON x.routeid = routes.id GROUP BY x.routeid, routes.length HAVING x.routeid IS NOT NULL AND routes.length IS NOT NULL; ";
        //System.out.println(queryString);
        Query q = getEntityManager().createNativeQuery(queryString);

        q.setParameter(1, providerID);
        q.setParameter(2, from, TemporalType.TIMESTAMP);
        q.setParameter(3, to, TemporalType.TIMESTAMP);

        StringBuilder json = new StringBuilder();
        ArrayList<IntervalTrafficData> lijst = new ArrayList<>();
        try {
            List<Object[]> rl = q.getResultList();
            if (rl != null) {
                for (Object[] o : rl) {
                    String str = null;
                    if (o[4] != null) {
                        str = o[4].toString();
                    }
                    IntervalTrafficData l = new IntervalTrafficData(Integer.parseInt(o[0].toString()), Integer.parseInt(o[1].toString()), Integer.parseInt(o[2].toString()), Integer.parseInt(o[3].toString()), str);

                    lijst.add(l);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getClass().getName() + " " + e.getMessage());
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
