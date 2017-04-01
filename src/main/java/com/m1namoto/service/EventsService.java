package com.m1namoto.service;

import java.util.List;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class EventsService {
    
    /**
     * Returns a list of events
     */
    public static List<Event> getList() {
        return DaoFactory.getEventsDAO().getList();
    }
    
    /**
     * Returns a list of user's events
     */
    public static List<Event> getListByUser(User user) {
        return DaoFactory.getEventsDAO().getListByUser(user.getId());
    }
    
    /**
     * Saves an event
     */
    public static Event save(Event event) {
    	return DaoFactory.getEventsDAO().save(event);
    }
    
    /**
     * Deletes user's events
     */
    public static void deleteUserEvents(User user) {
    	DaoFactory.getEventsDAO().deleteUserEvents(user);
    }
    
    /**
     * Deletes all events
     */
    public static void deleteAll() {
        DaoFactory.getEventsDAO().deleteAll();
    }

}
