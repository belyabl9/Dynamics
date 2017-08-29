package com.m1namoto.dao;

import com.m1namoto.domain.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SessionsDao extends GenericDAO<com.m1namoto.domain.Session> {

    private static final String SESSIONS_BY_DATE_QUERY = "FROM Session ORDER BY date";
    private static final String SESSIONS_BY_USER_QUERY = "FROM Session WHERE user_id = :user_id";
    private static final String DEL_SESSION_BY_ID_QUERY = "DELETE FROM Session WHERE id = :id";
    private static final String DELETE_SESSIONS_QUERY = "DELETE FROM Session";

    public SessionsDao(@NotNull SessionFactory factory) {
        super(com.m1namoto.domain.Session.class, factory);
    }

    /**
     * Returns a list of sessions
     */
    public List<com.m1namoto.domain.Session> getList() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(SESSIONS_BY_DATE_QUERY);
        return query.list();
    }
    
    /**
     * Returns a list of user sessions
     */
    public List<com.m1namoto.domain.Session> getList(@NotNull User user) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(SESSIONS_BY_USER_QUERY);
        query.setParameter("user_id", user.getId());
        return query.list();
    }
    
    /**
     * Deletes user sessions
     */
    public void removeAll(@NotNull User user) {
        for (com.m1namoto.domain.Session session : getList(user)) {
            delete(session);
        }
    }
    
    /**
     * Removes a session by id
     */
    public void remove(long id) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(DEL_SESSION_BY_ID_QUERY);
        query.setParameter("id", id);
        query.executeUpdate();
    }
    
    /**
     * Removes all sessions
     */
    public void removeAll() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(DELETE_SESSIONS_QUERY);
        query.executeUpdate();
    }

}
