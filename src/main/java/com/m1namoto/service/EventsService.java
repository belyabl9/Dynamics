package com.m1namoto.service;

import java.util.List;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class EventsService {
    public static List<Event> getList() {
        return DaoFactory.getEventsDAO().getList();
    }
    
    public static List<Event> getListByUser(User user) {
        return DaoFactory.getEventsDAO().getListByUser(user.getId());
    }
    
    public static Event save(Event event) {
    	return DaoFactory.getEventsDAO().save(event);
    }
    
    public static void deleteUserEvents(User user) {
    	DaoFactory.getEventsDAO().deleteUserEvents(user);
    }
    
    public static void deleteAll() {
        DaoFactory.getEventsDAO().deleteAll();
    }
    
    public static Event getKeyEvent(List<Event> events, String action, int code, int start) {
        for (int i = start; i < events.size(); i++) {
            Event event = events.get(i);
            if ( (event.getCode() == code) && (event.getAction().equals(action)) ) {
                return event;
            }
        }

        return null;
    }

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
