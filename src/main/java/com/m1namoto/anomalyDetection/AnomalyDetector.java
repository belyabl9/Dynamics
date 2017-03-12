package com.m1namoto.anomalyDetection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

class HF implements AnomalyDetection {
	private double val;
	
	public HF(double val) {
		this.val = val;
	}
	
	public double getValue() {
		return val;
	}
}

public class AnomalyDetector {
	final static Logger logger = Logger.getLogger(AnomalyDetector.class);

	private List<? extends AnomalyDetection> list;
	private double firstQuartile;
	private double thirdQuartile;
	private double interQuartileRange;

	private String description;
	
	public AnomalyDetector(List<? extends AnomalyDetection> list, String description) {
		this.description = description;
		this.list = list;
		Collections.sort(this.list, new Comparator<AnomalyDetection>() {

			@Override
			public int compare(AnomalyDetection o1, AnomalyDetection o2) {
				if (o1.getValue() < o2.getValue()) {
					return -1;
				}
		        if (o1.getValue() > o2.getValue()) {
		        	return 1;
		        }
		        return 0;
			}
			
		});
		
		int firstQuartileIndex = (list.size() * 25) / 100;
		int thirdQuartileIndex = (list.size() * 75) / 100;
		
		this.firstQuartile = list.get(firstQuartileIndex).getValue();
		this.thirdQuartile = list.get(thirdQuartileIndex).getValue();
		this.interQuartileRange = this.thirdQuartile - this.firstQuartile;
	}
	
	private boolean isAnomaly(AnomalyDetection obj) {
		if ((obj.getValue() > (this.thirdQuartile + (1.5 * this.interQuartileRange))) ||
			(obj.getValue() < (this.firstQuartile - (1.5 * this.interQuartileRange))) ) {
			return true;
		}

		return false;
	}
	
	public double getFirstQuartile() {
		return firstQuartile;
	}
	
	public double getThridQuartile() {
		return thirdQuartile;
	}
	
	public double getInterQuartileRange() {
		return interQuartileRange;
	}
	
	public void removeAnomalies() {
		logger.info(description);
		for (Iterator<? extends AnomalyDetection> iterator = this.list.iterator(); iterator.hasNext();) {
			AnomalyDetection obj = iterator.next();
			if (isAnomaly(obj)) {
				logger.info("Anomaly: " + obj.getValue());
				iterator.remove();
			}
		}
	}

}
