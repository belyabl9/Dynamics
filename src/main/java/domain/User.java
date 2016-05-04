package domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import service.Features;
import service.Sessions;

@Entity
@Table(name = "Users")
public class User extends DomainSuperClass implements Serializable {

	@Column(name = "name")
	private String name;

	@Column(name = "login")
    private String login;
    
    @Column(name = "password")
    private String password;
    
    public User() {}

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
		List<Session> sessions = Sessions.getSessionsByUser(id);
		return (sessions != null) ? sessions.size() : 0;
	}
	
	public double getMeanKeypressTime() {
		List<Session> sessions = Sessions.getSessionsByUser(id);
		if (sessions == null) {
			return 0;
		}
		double keyTime = 0,
			   n = 0;
		for (Session session : sessions) {
			keyTime += Features.getMeanKeyTime(session.getEvents());
			n++;
		}
		
		return keyTime/n;
	}
	
	public double getMeanTimeBetweenKeys() {
		List<Session> sessions = Sessions.getSessionsByUser(id);
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
}