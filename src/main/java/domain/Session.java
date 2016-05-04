package domain;

import java.util.ArrayList;
import java.util.List;

public class Session {

	private List<Event> events = new ArrayList<Event>();
	private int errRate;

	public Session(List<Event> events) {
		this.events = events;
	}
	
	public Session() {}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
	public int getErrRate() {
		return errRate;
	}

	public void setErrRate(int errRate) {
		this.errRate = errRate;
	}
	
	public String toString() {
		char[] symbols = new char[events.size()]; 
		for (int i = 0; i < events.size(); i++) {
			symbols[i] = (char) events.get(i).getCode();
		}
		
		return new String(symbols);
	}
}
