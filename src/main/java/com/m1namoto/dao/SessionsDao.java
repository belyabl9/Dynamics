package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.m1namoto.domain.User;

public class SessionsDao extends GenericDAO<com.m1namoto.domain.Session> {

    public SessionsDao(SessionFactory factory) {
        super(com.m1namoto.domain.Session.class, factory);
    }

    public List<com.m1namoto.domain.Session> getList() {
        String hql = "FROM Session ORDER BY date";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        List<com.m1namoto.domain.Session> sessions = query.list();

        return sessions;
    }
    
    public List<com.m1namoto.domain.Session> getUserSessions(User user) {
        String hql = "FROM Session WHERE user_id = :user_id";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setParameter("user_id", user.getId());
        List<com.m1namoto.domain.Session> sessions = query.list(); 

        return sessions;
    }
    
    public void deleteUserSessions(User user) {
        List<com.m1namoto.domain.Session> sessions = getUserSessions(user);
        for (com.m1namoto.domain.Session session : sessions) {
            delete(session);
        }
    }
    
    public void deleteSessionById(long id) {
        String hql = "DELETE FROM Session WHERE id = :id";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setParameter("id", id);
        query.executeUpdate();
    }
    
    public void deleteAll() {
        Session session = getFactory().getCurrentSession();
        String hql = "DELETE FROM Session";
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }

}
