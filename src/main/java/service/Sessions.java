package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.Event;
import domain.Session;

public class Sessions {

	public static List<Session> getSessions(List<Event> events) {
		List<Session> sessions = new ArrayList<Session>();
	    
		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.getAction() == Event.SESSION_START) {
				List<Event> sessionEvents = new ArrayList<Event>();
			
				int j = i + 1;
				int errCnt = 0;
				event = events.get(j);
				while (event != null && event.getAction() != Event.SESSION_END) {
					sessionEvents.add(event);
					if (event.getCode() == Event.BACKSPACE_CODE) {
						errCnt++;
					}
					j++;
					if (j == events.size()) {
						break;
					}
					event = events.get(j);
				}
				i = j;
				if (sessionEvents.size() > 0) {
					Session session = new Session(sessionEvents);
					System.out.println(session);
					if (sessionEvents.size() > 0) {
						session.setErrRate( ( errCnt/sessionEvents.size() ) * 100);
					}
					sessions.add(session) ;
				}
			}
		}
		
		return sessions;
	}
	
	public static Map<Long, List<Event>> getEventsPerUser() {
		Map<Long, List<Event>> userEventsMap = new HashMap<Long, List<Event>>();
		List<Event> allEvents = Events.getList();
		for (int i = 0; i < allEvents.size(); i++) {
			Event event = allEvents.get(i);
			long userId = event.getUser().getId();
			if (!userEventsMap.containsKey(userId)) {
				userEventsMap.put(userId, new ArrayList<Event>());
			}
			List<Event> userEvents = userEventsMap.get(userId);
			userEvents.add(event);
		}
		
		return userEventsMap;
	}
	
	public static Map<Long, List<Session>> getSessionsPerUser() {
		Map<Long, List<Session>> sessionsPerUser = new HashMap<Long, List<Session>>();
		
		Map<Long, List<Event>> userEventsMap = getEventsPerUser();
		
		for (Long userId : userEventsMap.keySet()) {
			sessionsPerUser.put(userId, getSessions(userEventsMap.get(userId)));
		}
		
		return sessionsPerUser;
	}
	
	public static List<Session> getSessionsByUser(long userId) {
		Map<Long, List<Session>> sessionsPerUser = getSessionsPerUser();

		return sessionsPerUser.get(userId); 
	}
	
}
