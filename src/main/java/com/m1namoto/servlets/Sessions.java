package com.m1namoto.servlets;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.dao.DaoFactory;
import com.m1namoto.dao.EventsDao;
import com.m1namoto.dao.UsersDao;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;

/**
 * Servlet implementation class Sessions
 */
@WebServlet("/saveSession")
public class Sessions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Sessions() {
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
	    String login = request.getParameter("identity");
        String session = request.getParameter("session");
        String eventsJson = request.getParameter("events");
        
        UsersDao usersDao = DaoFactory.getUsersDAO();
        EventsDao eventsDao = DaoFactory.getEventsDAO();
        User user = usersDao.findByLogin(login);
        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> events = new Gson().fromJson(eventsJson, type);
        for (Event event : events) {
            event.setSession(session);
            event.setUser(user);
            eventsDao.save(event);
        }
        
	}

}
