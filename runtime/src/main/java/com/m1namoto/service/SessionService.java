package com.m1namoto.service;

import com.m1namoto.dao.DaoFactory;
import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SessionService {

    private static final String INVALID_SESSION_ID = "Session id must be >= 0.";

    private SessionService() {}

    private static class LazyHolder {
        static final SessionService INSTANCE = new SessionService();
    }
    public SessionService getInstance() {
        return SessionService.LazyHolder.INSTANCE;
    }

    /**
     * Returns a list of sessions
     */
    public static List<Session> getList() {
        return DaoFactory.getSessionsDAO().getList();
    }

    /**
     * Returns a list of user's sessions
     */
    public static List<Session> getList(@NotNull User user) {
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
	
}
