package service;

import java.util.ArrayList;
import java.util.List;

import domain.Event;

public class Features {
	
	private static Event getKeyEvent(List<Event> events, int action, int code, int start) {
		for (int i = start + 1; i < events.size(); i++) {
			Event event = events.get(i);
	        if ( (event.getCode() == code) && (event.getAction() == action) ) {
	        	return event;
	        }
		}
		
		return null;
	}
	
	private static Event getKeyEvent(List<Event> events, int action, int start) {
		for (int i = start + 1; i < events.size(); i++) {
			Event event = events.get(i);
	        if (event.getAction() == action) {
	        	return event;
	        }
		}
		
		return null;
	}
	
	public static double getMeanTimeBetweenKeys(List<Event> events) {
		List<Double> timeDiffs = getTimeBetweenKeysList(events);
		double sum = 0;
		int n = 0;
		for (int i = 0; i < timeDiffs.size(); i++, n++) {
			sum += timeDiffs.get(i);
		}
		
		return sum/n;
	}
	
	public static double getMeanKeyTime(List<Event> events) {
		List<Double> timeDiffs = getKeyTimeList(events);
		double sum = 0;
		int n = 0;
		for (int i = 0; i < timeDiffs.size(); i++, n++) {
			sum += timeDiffs.get(i);
		}
		
		return sum/n;
	}
	
	public static List<Double> getKeyTimeList(List<Event> events) {
		List<Double> timeDiffs = new ArrayList<Double>();
		
		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.getAction() == Event.ACTION_DOWN) {
				int code = event.getCode();
				Event keyupEvent = getKeyEvent(events, Event.ACTION_UP, code, i);
				if (keyupEvent != null) {
					double timeDiff = keyupEvent.getTime() - event.getTime();
					// Anomalies
					if (timeDiff > 120) {
						continue;
					}
					timeDiffs.add(timeDiff);
				}
			}
		}
		
		return timeDiffs;
	}
	
	public static List<Double> getTimeBetweenKeysList(List<Event> events) {
		List<Double> timeDiffs = new ArrayList<Double>();

		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			if (event.getAction() == Event.ACTION_UP) {
				Event keydownEvent = getKeyEvent(events, Event.ACTION_DOWN, i);
				if (keydownEvent != null) {
					double timeDiff = keydownEvent.getTime() - event.getTime();
					if (timeDiff > 1000) {
						continue;
					}
					timeDiffs.add(timeDiff);
				}
			}
		}
		
		return timeDiffs;
	}
}
