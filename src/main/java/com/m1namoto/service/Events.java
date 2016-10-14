package com.m1namoto.service;

import java.util.List;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

public class Events {
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
}
