package dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import domain.Event;

public class EventsDao extends GenericDAO<Event> {

    public EventsDao(SessionFactory factory) {
        super(Event.class, factory);
    }

    public List<Event> getList() {
        return (List<Event>) findAll();
    }

    public List<Event> getListByTime() {
    	String hql = "FROM Event ORDER BY time";
    	Query query = getFactory().openSession().createQuery(hql);
    	
    	return query.list();
    }

    public void deleteUserEvents(long userId) {
    	Session session = getFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	Query query = session.createQuery("DELETE Event WHERE user_id = :id");
    	query.setParameter("id", userId);
    	 
    	query.executeUpdate();
    	transaction.commit();
    }
    
    public Event createEvent(Event event) {
        return save(event);
    }
}
