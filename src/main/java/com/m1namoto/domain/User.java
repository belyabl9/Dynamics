package com.m1namoto.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.m1namoto.service.Features;
import com.m1namoto.service.Sessions;

@Entity
@Table(name = "Users")
public class User extends DomainSuperClass implements Serializable {

    public static int USER_TYPE_ADMIN = 0;
    public static int USER_TYPE_REGULAR = 1;
    
	@Column(name = "name")
	private String name;

	@Column(name = "login", unique = true)
    private String login;
    
    @Column(name = "password")
    private String password;

    @Column(name = "userType", nullable=false)
    private int userType;

    @Column(name = "authenticatedCnt", columnDefinition = "int default 0", nullable=false)
    private int authenticatedCnt;

    public User() {}

    public int getAuthenticatedCnt() {
        return authenticatedCnt;
    }

    public void setAuthenticatedCnt(int authenticatedCnt) {
        this.authenticatedCnt = authenticatedCnt;
    }
    
    public String getFirstName() {
    	return name.split(" +")[0];
    }
    
    public String getSurname() {
    	return name.split(" +")[1];
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
    
	public int getSessionsCount() {
		List<Session> sessions = Sessions.getSessionsByUser(this);
		return (sessions != null) ? sessions.size() : 0;
	}
	
	public double getMeanKeypressTime() {
		List<Session> sessions = Sessions.getSessionsByUser(this);
		if (sessions == null) {
			return 0;
		}
		double keyTime = 0,
			   n = 0;
		for (Session session : sessions) {
			keyTime += Features.getMeanKeyPressTime(session.getEvents());
			n++;
		}
		
		return keyTime/n;
	}
	
	public double getMeanTimeBetweenKeys() throws Exception {
		List<Session> sessions = Sessions.getSessionsByUser(this);
		if (sessions == null) {
			return 0;
		}
		double keyTime = 0,
			   n = 0;
		for (Session session : sessions) {
			keyTime += Features.getMeanTimeBetweenKeys(session.getEvents());
			n++;
		}
		
		return keyTime/n;
	}
	
    public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}
	
	public static void main(String[] args) {
        Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        m.put(1, m.get(1) + 1);
        
    }
}