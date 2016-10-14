package com.m1namoto.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.m1namoto.domain.Event;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;

public class Sessions {
	
    private static Map<String, Session> getSessionsMap() {
        Map<String, Session> sessionsMap = new HashMap<String, Session>();
        for (Event event : Events.getList()) {
            String sessionName = event.getSession();
            
            if (!sessionsMap.containsKey(sessionName)) {
                sessionsMap.put(sessionName, new Session(sessionName, new ArrayList<Event>(), event.getUser()));
            }
            
            sessionsMap.get(sessionName).addEvent(event);
        }
        
        return sessionsMap;
    }

    public static Map<Long, List<Session>> getSessionsPerUser() {
        Map<Long, List<Session>> sessionsPerUserMap = new HashMap<Long, List<Session>>();
        Map<String, Session> sessionsMap = getSessionsMap();
        
        for (Session session : sessionsMap.values()) {
            long userId = session.getUser().getId();
            if (!sessionsPerUserMap.containsKey(userId)) {
                sessionsPerUserMap.put(userId, new ArrayList<Session>());
            }
            sessionsPerUserMap.get(userId).add(session);
        }
        
        return sessionsPerUserMap;
    }
    
	public static List<Session> getSessionsByUser(User user) {
	    List<Event> events = Events.getListByUser(user);
	    Map<String, List<Event>> sessionsMap = new HashMap<String, List<Event>>();

	    for (Event event : events) {
	        String sessionName = event.getSession();
	        if (!sessionsMap.containsKey(sessionName)) {
	            sessionsMap.put(sessionName, new ArrayList<Event>());
	        }
	        sessionsMap.get(sessionName).add(event);
	    }
	    
	    List<Session> sessions = new ArrayList<Session>();
	    for (String sessionName : sessionsMap.keySet()) {
	        sessions.add(new Session(sessionName, sessionsMap.get(sessionName), user));
	    }
	    
	    return sessions;
	}
	
}
