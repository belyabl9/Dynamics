package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

public class EventsDao extends GenericDAO<Event> {

    private static final String EVENTS_QUERY = "FROM Event ORDER BY time";
    private static final String EVENTS_BY_USER_QUERY = "FROM Event WHERE user_id = :id ORDER BY time";
    private static final String DELETE_USER_EVENTS_QUERY = "DELETE Event WHERE user_id = :id";
    private static final String DELETE_EVENTS_QUERY = "DELETE FROM Event";

    public EventsDao(@NotNull SessionFactory factory) {
        super(Event.class, factory);
    }

    /**
     * Returns a list of events ordered by timestamp
     */
    public List<Event> getList() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(EVENTS_QUERY);
        return query.list();
    }
    
    /**
     * Returns a list of user events by id
     */
    public List<Event> getList(long userId) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(EVENTS_BY_USER_QUERY);
        query.setParameter("id", userId);
        return query.list();
    }

    /**
     * Removes user events
     */
    public void removeAll(@NotNull User user) {
        Session session = getFactory().getCurrentSession(); 
    	Query query = session.createQuery(DELETE_USER_EVENTS_QUERY);
    	query.setParameter("id", user.getId());
    	query.executeUpdate();
    }
    
    /**
     * Creates an event
     */
    public Event createEvent(@NotNull Event event) {
        return save(event);
    }
    
    /**
     * Removes all events
     */
    public void removeAll() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(DELETE_EVENTS_QUERY);
        query.executeUpdate();
    }
}
