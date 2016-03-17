/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entitys.service;

import entitys.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Piet
 */
public abstract class AbstractFacade<T> {
    private Class<T> entityClass;
    protected Properties prop;
    private String XE;

    public AbstractFacade(Class<T> entityClass){
        this.entityClass = entityClass;    
        prop = new Properties();
        try{
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("entitys/service/entitydatabase.properties");
            if(propsFile == null){
                System.out.println("entitys/service/entitydatabase.properties kon niet geladen worden.");
            }
            else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e) {
            System.out.println("entitys/service/entitydatabase.properties niet gevonden.");
        }catch( IOException ee){
            System.out.println("entitys/service/entitydatabase.properties kon niet geladen worden.");
        }
        if(entityClass.equals(Provider.class)){
            XE = "PE";
        }
        else if(entityClass.equals(Route.class)){
            XE = "RE";
        }
        else if(entityClass.equals(Trafficdata.class)){
            XE = "DE"; 
        }
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }
    
    public T findByName(String naam) {
        Query q = getEntityManager().createQuery(prop.getProperty("SELECT_" + XE + "_NAME"));
        q.setParameter("name", naam);
        return (T) q.getSingleResult();
    }
    
    public List<T> findAll() {
        return getEntityManager().createQuery(prop.getProperty("SELECT_" + XE)).getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
}
