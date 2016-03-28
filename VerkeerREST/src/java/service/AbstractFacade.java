/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Robin
 * @param <T>
 */
public abstract class AbstractFacade<T> {

    private static final Logger log = Logger.getLogger(AbstractFacade.class);
    private Class<T> entityClass;
    protected Properties prop;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
        try{
            prop = new Properties();
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream("service/entities.properties");
            if(propsFile == null){
                log.error("service/entities.properties kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
        }catch( FileNotFoundException e) {
            log.error("service/entities.properties niet gevonden.");
        }catch( IOException ee){
            log.error("service/entities.properties niet gevonden.");
        }
    }

    protected abstract EntityManager getEntityManager();

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }
    
}
