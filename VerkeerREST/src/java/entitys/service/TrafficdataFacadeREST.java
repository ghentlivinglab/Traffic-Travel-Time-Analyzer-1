/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entitys.service;

import entitys.Trafficdata;
import entitys.TrafficdataPK;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Piet
 */
@Stateless
@Path("trafficdata")
public class TrafficdataFacadeREST extends AbstractFacade<Trafficdata> {

    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;

    private TrafficdataPK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;routeID=routeIDValue;providerID=providerIDValue;timestamp=timestampValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        entitys.TrafficdataPK key = new entitys.TrafficdataPK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> routeID = map.get("routeID");
        if (routeID != null && !routeID.isEmpty()) {
            key.setRouteID(new java.lang.Integer(routeID.get(0)));
        }
        java.util.List<String> providerID = map.get("providerID");
        if (providerID != null && !providerID.isEmpty()) {
            key.setProviderID(new java.lang.Integer(providerID.get(0)));
        }
        java.util.List<String> timestamp = map.get("timestamp");
        if (timestamp != null && !timestamp.isEmpty()) {
            key.setTimestamp(new java.util.Date(timestamp.get(0)));
        }
        return key;
    }

    public TrafficdataFacadeREST() {
        super(Trafficdata.class);
    }

    @POST
    @Override
    @Consumes({"application/json"})
    public void create(Trafficdata entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/json"})
    public void edit(@PathParam("id") PathSegment id, Trafficdata entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        entitys.TrafficdataPK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({"application/json"})
    public Trafficdata find(@PathParam("id") PathSegment id) {
        entitys.TrafficdataPK key = getPrimaryKey(id);
        return super.find(key);
    }

    /**
     *
     * @param routeId
     * @param providerId
     * @param timestampString
     * @return
     * @throws ParseException
     */
    @GET
    @Path("providerid={providerId}/routeid={routeId}/timestamp={timestampString}")
    @Produces({"application/json"})
    public Trafficdata findDataById(@PathParam("routeId") Integer routeId, @PathParam("providerId") Integer providerId, @PathParam("timestampString") String timestampString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedTimestamp = dateFormat.parse(timestampString);
        Timestamp timestamp = new java.sql.Timestamp(parsedTimestamp.getTime());
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_DE_ID"));
        q.setParameter("providerId", providerId);
        q.setParameter("routeId", routeId);
        q.setParameter("timestamp", timestamp);

        return (Trafficdata) q.getSingleResult();
    }

    /**
     *
     * @param routeId
     * @param providerId
     * @param fromString
     * @param toString
     * @return
     * @throws ParseException
     */
    @GET
    @Path("providerid={providerId}/routeid={routeId}/from={timestampFrom}/to={timestampTo}")
    @Produces({"application/json"})
    public List<Trafficdata> findDataBetween(@PathParam("routeId") Integer routeId, @PathParam("providerId") Integer providerId,
            @PathParam("timestampFrom") String fromString, @PathParam("timestampTo") String toString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedTimestampFrom = dateFormat.parse(fromString);
        Timestamp timestampFrom = new java.sql.Timestamp(parsedTimestampFrom.getTime());
        Date parsedTimestampTo = dateFormat.parse(toString);
        Timestamp timestampTo = new java.sql.Timestamp(parsedTimestampTo.getTime());
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_DE_BETWEEN"));
        q.setParameter("providerId", providerId);
        q.setParameter("routeId", routeId);
        q.setParameter("timestampFrom", timestampFrom);
        q.setParameter("timestampTo", timestampTo);

        return q.getResultList();
    }

    /* ------ Opvragen met Interval - nog te doen ------------  */
    @GET
    @Path("providerid={providerid}/routeid={routeid}/from={timestampFrom}/tox={timestampTo}")
    @Produces({"application/json"})
    public List<Trafficdata> findAvgDataBetween(@PathParam("routeId") Integer routeId, @PathParam("providerId") Integer providerId,
            @PathParam("timestampFrom") String fromString, @PathParam("timestampTo") String toString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedTimestampFrom = dateFormat.parse(fromString);
        Timestamp timestampFrom = new java.sql.Timestamp(parsedTimestampFrom.getTime());
        Date parsedTimestampTo = dateFormat.parse(toString);
        Timestamp timestampTo = new java.sql.Timestamp(parsedTimestampTo.getTime());
        
        List<Object[]> objects = getEntityManager().createNativeQuery("select "
                + "timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%30 minute, "
                + "avg(traveltime) "
                + "from trafficdata "
                + "where providerID = ? and routeID = ? and timestamp between ? and ? "
                + "group by timestamp - interval extract(second from timestamp) second - interval extract(minute from timestamp)%30 minute")
        .setParameter(1, routeId)
        .setParameter(2, routeId)
        .setParameter(3, timestampFrom)
        .setParameter(4, timestampTo)
        .getResultList();
        
        System.out.println(objects.size());
        List<Trafficdata> data = new ArrayList<>();
        for (Object[] obj : objects) {
            System.out.println("hallo2");
            Date parsedTimestampAvg = dateFormat.parse((String) obj[0]);
            Timestamp timestampAvg = new java.sql.Timestamp(parsedTimestampAvg.getTime());
            Trafficdata de = new Trafficdata(routeId, providerId, timestampAvg);
            de.setTraveltime((Integer) obj[1]);
            data.add(de);
        }
        return data;
    }
    /* ------------------------------------------------------------ */
    
    @GET
    @Override
    @Produces({"application/json"})
    public List<Trafficdata> findAll() {
        return super.findAll();
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
