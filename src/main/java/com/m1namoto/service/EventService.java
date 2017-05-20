package com.m1namoto.service;

import java.util.List;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

public class EventService {
    
    /**
     * Returns a list of events
     */
    public static List<Event> getList() {
        return DaoFactory.getEventsDAO().getList();
    }
    
    /**
     * Returns a list of user's events
     */
    public static List<Event> getList(@NotNull User user) {
        return DaoFactory.getEventsDAO().getList(user.getId());
    }
    
    /**
     * Saves an event
     */
    public static Event save(@NotNull Event event) {
    	return DaoFactory.getEventsDAO().save(event);
    }
    
    /**
     * Removes user's events
     */
    public static void removeAll(@NotNull User user) {
    	DaoFactory.getEventsDAO().removeAll(user);
    }
    
    /**
     * Removes all events
     */
    public static void removeAll() {
        DaoFactory.getEventsDAO().removeAll();
    }

}
