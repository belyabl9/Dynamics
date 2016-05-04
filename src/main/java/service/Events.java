package service;

import java.util.List;

import dao.DaoFactory;
import domain.Event;
import domain.User;

public class Events {
    public static List<Event> getList() {
        return DaoFactory.getEventsDAO().getListByTime();
    }
    
    public static Event save(Event event) {
    	return DaoFactory.getEventsDAO().save(event);
    }
    
    public static void deleteUserEvents(long userId) {
    	DaoFactory.getEventsDAO().deleteUserEvents(userId);
    }
}
