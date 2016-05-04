package servlets;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import domain.Event;
import domain.Session;
import domain.User;
import service.DynamicsInstance;
import service.Events;
import service.Features;
import service.NeuralNetwork;
import service.Sessions;
import service.Users;

/**
 * Servlet implementation class Auth
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Auth() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	/*
    	
    	response.setContentType("application/json");       
 
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        
        */
        
        String stat = request.getParameter("stat");
        String login = request.getParameter("login");
        String password = request.getParameter("password");

    	if (login.isEmpty() || password.isEmpty() || stat.isEmpty()) {
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login or password is empty");
    		return;
    	}
        
        System.out.println(stat);
        System.out.println(login);
        System.out.println(password);
        
    	User user = Users.findByLogin(login);
    	if (user == null) {
    		System.out.println("User does not exist");
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Can't find user");
    		return;
    	}
    	
    	if (!user.getPassword().equals(password)) {
    		System.out.println("Wrong password");
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Can't find user");
    		return;
    	}
    	
    	System.out.println("User: " + user.getId());
        
        Type itemsListType = new TypeToken<List<Event>>() {}.getType();
        Gson gson = new Gson();
        List<Event> statList = gson.fromJson(stat, itemsListType);
		
        if (statList.isEmpty()) {
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Keystroke dynamics is empty");
    		return;
        }
        
        List<Session> sessions = Sessions.getSessions(statList);
        if (statList.isEmpty()) {
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "There are no keystroke dynamics sessions");
    		return;
        }
        
        int predictedClass = -1;
        Map<Long, List<Event>> eventsPerUser = Sessions.getEventsPerUser();
		List<Event> userEvents = eventsPerUser.get(user.getId());
		if (userEvents != null && !userEvents.isEmpty()) {
			Session session = sessions.get(0);
	        List<Event> sessionEvents = session.getEvents();

	        double meanKeyTime = Features.getMeanKeyTime(sessionEvents);
	        double meanTimeBetweenKeys = Features.getMeanTimeBetweenKeys(sessionEvents);
	        
	        System.out.println("Mean Key Time: " + meanKeyTime);
	        System.out.println("Mean Time Between Keys: " + meanTimeBetweenKeys);
	        
	        NeuralNetwork network;
			try {
				network = new NeuralNetwork();
				List<Double> lst = new ArrayList<Double>();
				lst.add(meanKeyTime);
				lst.add(meanTimeBetweenKeys);
				DynamicsInstance instance = new DynamicsInstance(lst);
				predictedClass = network.getClassForInstance(instance);
				System.out.println("Predicted Class: " +  predictedClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
        
		if (userEvents == null || userEvents.isEmpty()) {
	        for (Event event : statList) {
	        	event.setUser(user);
	        	Events.save(event);
	        }
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} else if (predictedClass == user.getId()) {
	        for (Event event : statList) {
	        	event.setUser(user);
	        	Events.save(event);
	        }
			response.setStatus(HttpServletResponse.SC_OK);
			return;
        }

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

}
