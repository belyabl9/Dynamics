package com.m1namoto.page;

import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;

import java.util.List;

public class UserSessionsPageData extends PageData {
    private List<Session> sessions;
    private User user;

    public UserSessionsPageData(List<Session> sessions, User user) {
        this.sessions = sessions;
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public List<Session> getSessions() {
        return sessions;
    }
}
