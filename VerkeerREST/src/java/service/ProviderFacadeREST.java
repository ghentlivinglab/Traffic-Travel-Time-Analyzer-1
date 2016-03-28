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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
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

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Provider> processRequest(@QueryParam("id") Integer providerID, @QueryParam("name") String providerName) {
        if(providerID!= null){
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_PE_ID"));
            q.setParameter("providerID", providerID);
            return q.getResultList();
        }
        if(providerName != null){
            Query q = getEntityManager().createQuery(prop.getProperty("SELECT_PE_NAME"));
            q.setParameter("name", providerName);
            return q.getResultList();
        }
        else{
            return super.findAll();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
