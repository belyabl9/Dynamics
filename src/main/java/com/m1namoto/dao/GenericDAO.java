package com.m1namoto.dao;

import java.util.Collection;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.DomainSuperClass;

public abstract class GenericDAO<T extends DomainSuperClass> {

    private SessionFactory factory;
    protected Class<T> persistentClass;

    private static final String QUERY_SELECT_ALL = "from %s";

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

    public T findById(long id) throws PersistenceException {
        Session session = factory.openSession();
        session.getTransaction().begin();
        T savedEntity = null;
        try {
            savedEntity = (T) session.get(persistentClass, id);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
        return savedEntity;
    }

    public void delete(T entity) throws PersistenceException {
        if (entity == null) {
            throw new PersistenceException("Entity for deleting cannot be null!");
        }
        Session session = factory.openSession();
        session.getTransaction().begin();
        try {
            session.delete(entity);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new PersistenceException(e);
        } finally {
            session.close();
        }
    }

    public T save(T entity) throws PersistenceException {
        if (entity == null) {
            throw new PersistenceException("Entity for saving cannot be null!");
        }
        Session session = factory.openSession();
        session.getTransaction().begin();
        T savedEntity = null;
        try {
            System.out.println("entity id: " + entity.getId());
            if (entity.getId() == 0) {
                session.save(entity);
                savedEntity = entity;
            } else {
                savedEntity = (T) session.merge(entity);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
            throw new PersistenceException(e);
        } finally {
            session.close();
        }
        return savedEntity;
    }

    protected <REZ> REZ executeQuery(String queryOrQueryName,
            boolean singleResult, Map<String, Object> args) throws PersistenceException {
        Session session = factory.openSession();
        session.getTransaction().begin();
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
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new PersistenceException(e);
        } finally {
            session.close();
        }
        return result;
    }

}