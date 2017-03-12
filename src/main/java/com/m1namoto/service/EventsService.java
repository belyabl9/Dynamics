package com.m1namoto.service;

import java.util.List;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class EventsService {
    
    /**
     * Returns a list of events
     * @return List of events
     */
    public static List<Event> getList() {
        return DaoFactory.getEventsDAO().getList();
    }
    
    /**
     * Returns a list of user's events
     * @param user
     * @return List of user's events
     */
    public static List<Event> getListByUser(User user) {
        return DaoFactory.getEventsDAO().getListByUser(user.getId());
    }
    
    /**
     * Saves an event
     * @param event
     * @return Saved event
     */
    public static Event save(Event event) {
    	return DaoFactory.getEventsDAO().save(event);
    }
    
    /**
     * Deletes user's events
     * @param user
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
    
    /**
     * Finds an event with specific code and action
     * in a list of events starting from specified position
     * @param events - list of events
     * @param action - [press, release]
     * @param code - key code
     * @param start - Position to start from in events list
     * @return Event or null
     */
    public static Event getKeyEvent(List<Event> events, String action, int code, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if ( (event.getCode() == code) && (event.getAction().equals(action)) ) {
                return event;
            }
        }

        return null;
    }

    /**
     * Finds an event with specific action
     * in a list of events starting from specified position
     * @param events - list of events
     * @param action - [press, release]
     * @param start - Position to start from in events list
     * @return Event or null
     */
    public static Event getKeyEvent(List<Event> events, String action, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getAction().equals(action)) {
                return event;
            }
        }

        return null;
    }
}
