package com.m1namoto.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

public class SessionService {

    public static final String INVALID_SESSION_ID = "Session id must be >= 0.";

    /**
     * Returns a list of sessions
     */
    public static List<Session> getList() {
        return DaoFactory.getSessionsDAO().getList();
    }

    /**
     * Returns a list of user's sessions
     */
    public static List<Session> getUserSessions(@NotNull User user) {
        return DaoFactory.getSessionsDAO().getList(user);
    }

    public static Session save(@NotNull Session session) {
        return DaoFactory.getSessionsDAO().save(session);
    }

    /**
     * Deletes user's sessions
     */
    public static void remove(@NotNull User user) {
        DaoFactory.getSessionsDAO().removeAll(user);
    }
    
    
    public static void remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(INVALID_SESSION_ID);
        }
        DaoFactory.getSessionsDAO().remove(id);
    }
    
    /**
     * Removes all sessions
     */
    public static void removeAll() {
        DaoFactory.getSessionsDAO().removeAll();
    }
    
    /**
     * Returns a map of sessions. Each sessions is represented by unique id
     * @return Map of sessions
     */
    private static Map<String, Session> getSessionsMap() {
        Map<String, Session> sessionsMap = new HashMap<String, Session>();
        for (Event event : EventService.getList()) {
            String sessionName = event.getSession();
            
            if (!sessionsMap.containsKey(sessionName)) {
                sessionsMap.put(sessionName, new Session(sessionName, new ArrayList<Event>(), event.getUser()));
            }
            
            sessionsMap.get(sessionName).addEvent(event);
        }
        
        return sessionsMap;
    }

    /**
     * Returns a map of sessions grouped by user
     * @return Map of sessions grouped by user
     */
    public static Map<Long, List<Session>> getSessionsPerUser() {
        ListMultimap<Long, Session> sessionsPerUserMap = ArrayListMultimap.create();

        //Map<Long, List<Session>> sessionsPerUserMap = new HashMap<Long, List<Session>>();
        Map<String, Session> sessionsMap = getSessionsMap();
        
        for (Session session : sessionsMap.values()) {
            long userId = session.getUser().getId();
            sessionsPerUserMap.put(userId, session);
        }
        
        return Multimaps.asMap(sessionsPerUserMap);
    }
	
}
