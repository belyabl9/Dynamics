package com.m1namoto.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;

public class SessionsService {
	
    public static List<Session> getList() {
        return DaoFactory.getSessionsDAO().getList();
    }

    public static List<Session> getUserSessions(User user) {
        return DaoFactory.getSessionsDAO().getUserSessions(user);
    }
    
    public static Session save(Session session) {
        return DaoFactory.getSessionsDAO().save(session);
    }

    public static void deleteUserSessions(User user) {
        DaoFactory.getSessionsDAO().deleteUserSessions(user);
    }
    
    public static void deleteById(long id) {
        DaoFactory.getSessionsDAO().deleteSessionById(id);
    }
    
    public static void deleteAll() {
        DaoFactory.getSessionsDAO().deleteAll();
    }
    
    private static Map<String, Session> getSessionsMap() {
        Map<String, Session> sessionsMap = new HashMap<String, Session>();
        for (Event event : EventsService.getList()) {
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
	
}
