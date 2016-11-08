package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class EventsDao extends GenericDAO<Event> {

    public EventsDao(SessionFactory factory) {
        super(Event.class, factory);
    }

    public List<Event> getList() {
        String hql = "FROM Event ORDER BY time";
        Query query = getFactory().openSession().createQuery(hql);
        
        return query.list();
    }
    
    public List<Event> getListByUser(long id) {
        String hql = "FROM Event WHERE user_id = :id ORDER BY time";
        Query query = getFactory().openSession().createQuery(hql);
        query.setParameter("id", id);
        
        return query.list();
    }

    public void deleteUserEvents(User user) {
    	Session session = getFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	Query query = session.createQuery("DELETE Event WHERE user_id = :id");
    	query.setParameter("id", user.getId());
    	 
    	query.executeUpdate();
    	transaction.commit();
    	session.close();
    }
    
    public Event createEvent(Event event) {
        return save(event);
    }
    
    public void deleteAll() {
        Session session = getFactory().openSession();
        session.getTransaction().begin();
        String hql = "DELETE FROM Event";
        Query query = session.createQuery(hql);
        query.executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
}
