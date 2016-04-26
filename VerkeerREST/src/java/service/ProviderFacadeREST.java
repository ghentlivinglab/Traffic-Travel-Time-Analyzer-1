/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import domain.Provider;
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
 * for path '/api/providers'
 *
 * @author Robin
 */
@Stateless
@Path("/providers")
public class ProviderFacadeREST extends AbstractFacade<Provider> {
    
    @PersistenceContext(unitName = "VerkeerRESTPU")
    private EntityManager em;
    
    public ProviderFacadeREST() {
        super(Provider.class);
    }

    /**
     * Processes GET HTTP-requests for path '/api/providers/{providerID}'
     * {providerID} must be an Integer value
     *
     * @param providerID id from the provider you want to fetch
     * @return a single provider, with the specified id, in JSON-format
     */
    @GET
    @Path("/{providerID}")
    @Produces({MediaType.APPLICATION_JSON})
    public String processId(@PathParam("providerID") Integer providerID) {
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_PE_ID"));
        q.setParameter("providerID", providerID);
        List<Provider> result = q.getResultList();
        if (!result.isEmpty()) {
            Provider p = result.get(0);
            return processSuccess(providerToJSON(p));
        } else {
            return processError("No provider found with id " + providerID + ".");
        }
    }

    /**
     * Processes GET HTTP-requests for path '/api/providers'
     * <p>
     * parameters must be separated from the path by a '?' sign, multiple
     * parameters must be separated from each other by an '&' sign.
     *
     * @param providerID id from the provider you want to fetch
     * @param providerName name from the provider you want to fetch
     * @return a single provider or a list of providers in JSON-format
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String processRequest(@QueryParam("id") Integer providerID, @QueryParam("name") String providerName) {
        if (providerID != null) {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_PE_ID"));
            q.setParameter("providerID", providerID);
            List<Provider> result = q.getResultList();
            if (!result.isEmpty()) {
                Provider p = result.get(0);
                return processSuccess(providerToJSON(p));
            } else {
                return processError("No provider found with id " + providerID + ".");
            }
        }
        if (providerName != null) {
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_PE_NAME"));
            q.setParameter("name", providerName);
            List<Provider> result = q.getResultList();
            if (!result.isEmpty()) {
                Provider p = result.get(0);
                return processSuccess(providerToJSON(p));
            } else {
                return processError("No provider found with name " + providerName + ".");
            }
        } else {
            List<Provider> providers = super.findAll();
            return processSuccess(providerListToJSON(providers));
        }
    }

    /**
     * parses a provider to a String in JSON-format
     *
     * @param p the provider to be parsed
     * @return String in JSON-format
     */
    public String providerToJSON(Provider p) {
        String delimiter = ",";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(p.getId());
        sb.append(delimiter).append("\"name\":\"").append(p.getName()).append("\"");
        sb.append(delimiter).append("\"weight\":").append(p.getWeight());
        sb.append("}");
        return sb.toString();
    }

    /**
     * parses a list of providers to a String in JSON-format
     *
     * @param providers the list of providers to be parsed
     * @return String in JSON-format
     */
    public String providerListToJSON(List<Provider> providers) {
        String delimiter = "";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Provider p : providers) {
            sb.append(delimiter).append(providerToJSON(p));
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
