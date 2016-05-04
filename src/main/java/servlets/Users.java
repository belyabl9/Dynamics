package servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import domain.User;

/**
 * Servlet implementation class Users
 */
@WebServlet("/users")
public class Users extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static String USERS_PAGE = "/users.jsp";
	private static String USER_PAGE = "/user.jsp";
	private static String SAVE_USER_ACTION = "save";
	private static String DEL_USER_ACTION = "del";
	private static String DEL_USER_SESSIONS = "deleteSessions";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Users() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("id");
		if (userId != null) {
			if (!userId.equals("New")) {
				User user = service.Users.findById(Long.parseLong(userId));
				request.setAttribute("user", user);
			}

			request.getRequestDispatcher(USER_PAGE).forward(request, response);
			return;
		}

		List<User> users = service.Users.getList();
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
				user = service.Users.findById(Long.parseLong(userId));
			}
			
			user.setName(request.getParameter("firstName") + " " + request.getParameter("surname"));
			user.setLogin(request.getParameter("login"));
			user.setPassword(request.getParameter("password"));
			user = service.Users.save(user);
			userId = String.valueOf(user.getId());
		} else if (action != null && action.equals(DEL_USER_ACTION)) {
			User user = service.Users.findById(Long.parseLong(userId));
			service.Users.del(user);
			response.sendRedirect(request.getContextPath() + "/users");
			return;
		} else if (action != null && action.equals(DEL_USER_SESSIONS)) {
			service.Events.deleteUserEvents(Long.parseLong(userId));
		}
		
		response.sendRedirect(request.getContextPath() + "/users?id=" + String.valueOf(userId));
	}

}
