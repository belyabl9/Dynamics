package com.m1namoto.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.m1namoto.domain.User;

/**
 * Servlet implementation class Users
 */
@WebServlet("/users")
public class UsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static String USERS_PAGE = "/users.jsp";
	private static String ADD_USER_PAGE = "/addUser.jsp";
	private static String USER_INFO_PAGE = "/userInfo.jsp";
	private static String SAVE_USER_ACTION = "save";
	private static String DEL_USER_ACTION = "del";
	private static String DEL_USER_SESSIONS = "deleteSessions";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UsersServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("id");
		if (userId != null) {
			if (!userId.equals("New")) {
				User user = com.m1namoto.service.Users.findById(Long.parseLong(userId));
				request.setAttribute("user", user);
				request.getRequestDispatcher(USER_INFO_PAGE).forward(request, response);
				return;
			}

			request.getRequestDispatcher(ADD_USER_PAGE).forward(request, response);
			return;
		}

		List<User> users = com.m1namoto.service.Users.getList();
		request.setAttribute("users", users);
		request.getRequestDispatcher(USERS_PAGE).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String userId = request.getParameter("id");
		
		if (action != null && action.equals(SAVE_USER_ACTION)) {
			User user;
			if (userId != null && userId.equals("New")) {
				user = new User();
			} else {
				user = com.m1namoto.service.Users.findById(Long.parseLong(userId));
			}
			
			user.setName(request.getParameter("firstName") + " " + request.getParameter("surname"));
			user.setLogin(request.getParameter("login"));
			user.setPassword(request.getParameter("password"));
			user.setUserType(Integer.parseInt(request.getParameter("userType")));
			user = com.m1namoto.service.Users.save(user);
			userId = String.valueOf(user.getId());
		} else if (action != null && action.equals(DEL_USER_ACTION)) {
			User user = com.m1namoto.service.Users.findById(Long.parseLong(userId));
			com.m1namoto.service.Users.del(user);
			response.sendRedirect(request.getContextPath() + "/users");
			return;
		} else if (action != null && action.equals(DEL_USER_SESSIONS)) {
		    User user = new User();
		    user.setId(Long.parseLong(userId));
			com.m1namoto.service.Events.deleteUserEvents(user);
		}
		
		response.sendRedirect(request.getContextPath() + "/users?id=" + String.valueOf(userId));
	}

}
