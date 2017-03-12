package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class EventsDao extends GenericDAO<Event> {

    public EventsDao(SessionFactory factory) {
        super(Event.class, factory);
    }

    /**
     * Returns a list of events
     * @return List of events
     */
    @SuppressWarnings("unchecked")
    public List<Event> getList() {
        String hql = "FROM Event ORDER BY time";
        Session session = getFactory().getCurrentSession(); 
        Query query = session.createQuery(hql);
        List<Event> events = query.list();
        
        return events;
    }
    
    /**
     * Returns a list of user events by id
     * @param id
     * @return List of user events
     */
    @SuppressWarnings("unchecked")
    public List<Event> getListByUser(long id) {
        String hql = "FROM Event WHERE user_id = :id ORDER BY time";
        Session session = getFactory().getCurrentSession(); 
        Query query = session.createQuery(hql);
        query.setParameter("id", id);
        List<Event> events = query.list();

        return events;
    }

    /**
     * Deletes user events
     * @param user
     */
    public void deleteUserEvents(User user) {
        Session session = getFactory().getCurrentSession(); 
    	Query query = session.createQuery("DELETE Event WHERE user_id = :id");
    	query.setParameter("id", user.getId());
    	query.executeUpdate();
    }
    
    /**
     * Create an event
     * @param event
     * @return Created event
     */
    public Event createEvent(Event event) {
        return save(event);
    }
    
    /**
     * Deletes all events
     */
    public void deleteAll() {
        Session session = getFactory().getCurrentSession();
        String hql = "DELETE FROM Event";
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }
}
