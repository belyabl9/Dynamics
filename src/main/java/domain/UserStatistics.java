package domain;

import java.util.ArrayList;
import java.util.List;

public class UserStatistics {
	private User user;
	private List<Double> keypressList = new ArrayList<Double>();

	private List<Double> betweenKeypressList = new ArrayList<Double>();
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public void addKeypressTime(double keypressTime) {
		keypressList.add(keypressTime);
	}
	
	public void addTimeBetweenKeypress(double timeBetweenKeypress) {
		betweenKeypressList.add(timeBetweenKeypress);
	}
	
	public List<Double> getKeypressList() {
		return keypressList;
	}
	
	public List<Double> getBetweenKeypressList() {
		return betweenKeypressList;
	}
}
