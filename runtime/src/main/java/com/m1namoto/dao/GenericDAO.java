package com.m1namoto.dao;

import com.m1namoto.domain.DomainSuperClass;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.Map;

public abstract class GenericDAO<T extends DomainSuperClass> {
    final static Logger logger = Logger.getLogger(GenericDAO.class);
    
    private SessionFactory factory;
    protected Class<T> persistentClass;

    private static final String QUERY_SELECT_ALL = "from %s";

    private static final String ENTITY_CAN_NOT_BE_NULL = "Entity can not be null";
    
    public GenericDAO(Class<T> persistentClass, SessionFactory factory) {
        super();
        this.persistentClass = persistentClass;
        this.factory = factory;
    }

    public SessionFactory getFactory() {
        return this.factory;
    }

    public Collection<T> findAll() throws PersistenceException {
        return executeQuery(String.format(QUERY_SELECT_ALL, persistentClass.getSimpleName()), false, null);
    }

    @SuppressWarnings("unchecked")
    public T findById(long id) throws PersistenceException {
        Session session = getFactory().getCurrentSession();
        T savedEntity = null;
        try {
            savedEntity = (T) session.get(persistentClass, id);
        } catch (Exception e) {
            logger.error(e);
        }

        return savedEntity;
    }

    public void delete(T entity) throws PersistenceException {
        if (entity == null) {
            throw new PersistenceException(ENTITY_CAN_NOT_BE_NULL);
        }
        Session session = getFactory().getCurrentSession();
        try {
            session.delete(entity);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public T save(T entity) throws PersistenceException {
        if (entity == null) {
            throw new PersistenceException(ENTITY_CAN_NOT_BE_NULL);
        }
        Session session = getFactory().getCurrentSession();
        T savedEntity = null;
        try {
            if (entity.getId() == 0) {
                session.save(entity);
                savedEntity = entity;
            } else {
                savedEntity = (T) session.merge(entity);
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }

        return savedEntity;
    }

    @SuppressWarnings("unchecked")
    protected <REZ> REZ executeQuery(String queryOrQueryName,
            boolean singleResult, Map<String, Object> args) throws PersistenceException {
        Session session = getFactory().getCurrentSession();
        REZ result;
        Query q;
        try {
            q = session.createQuery(queryOrQueryName);

            if (args != null)
                for (Map.Entry<String, Object> pair : args.entrySet()) {
                    q.setParameter(pair.getKey(), pair.getValue());
                }
            if (singleResult) {
                result = (REZ) q.uniqueResult();
            } else {
                result = (REZ) q.list();
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }

        return result;
    }
}