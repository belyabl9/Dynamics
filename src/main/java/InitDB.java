import dao.DaoFactory;
import domain.Event;
import domain.User;
import service.NeuralNetwork;
import service.Users;

public class InitDB {
	
	public static void main(String[] args) {
		/*
		
		User user = new User();
		user.setLogin("login");
		user.setPassword("password");
		user.setName("Name");
		
		user = DaoFactory.getUsersDAO().createUser(user);
		
		Event event = new Event();
		event.setAction(1);
		event.setCode(2);
		event.setOrientation((short)1);
		event.setTime(123456789);
		event.setUser(user);
		
		Event event2 = new Event();
		event2.setAction(1);
		event2.setCode(2);
		event2.setOrientation((short)1);
		event2.setTime(123456789);
		event2.setUser(user);
		
		DaoFactory.getEventsDAO().createEvent(event);
		DaoFactory.getEventsDAO().createEvent(event2);
		
		User u = Users.findByLogin("login");
		if (u != null) {
			System.out.println(u.getPassword());
		}
		*/
		//System.out.println(user.getId());
	
	}
	
}
