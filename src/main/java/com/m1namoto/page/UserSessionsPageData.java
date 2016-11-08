package com.m1namoto.page;

import java.util.List;

import com.m1namoto.domain.Session;
import com.m1namoto.domain.User;

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
