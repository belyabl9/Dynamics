package com.m1namoto.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.UsersService;
import org.jetbrains.annotations.NotNull;

/**
 * Servlet implementation class Users
 */
@WebServlet("/users")
public class UsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static String USERS_PAGE = "/users.jsp";
	private static String ADD_USER_PAGE = "/addUser.jsp";
	private static String USER_INFO_PAGE = "/userInfo.jsp";

    private enum Action {
        SAVE_USER("save"),
        DEL_USER("del"),
        DEL_USER_SESSIONS("deleteSessions"),;

        @NotNull
        private final String name;

        Action(@NotNull String name) {
            this.name = name;
        }

        public static Action fromString(@NotNull String name) {
            for (Action action : values()) {
                if (action.getName().equals(name)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Invalid action name: " + name);
        }

        @NotNull
        public String getName() {
            return name;
        }
    }

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
				Optional<User> userOpt = UsersService.findById(Long.parseLong(userId));
				if (userOpt.isPresent()) {
					request.setAttribute("user", userOpt.get());
					request.getRequestDispatcher(USER_INFO_PAGE).forward(request, response);
					return;
				}
			}

			request.getRequestDispatcher(ADD_USER_PAGE).forward(request, response);
			return;
		}

		List<User> users = UsersService.getList();
		request.setAttribute("users", users);
		request.getRequestDispatcher(USERS_PAGE).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String actionParam = request.getParameter("action");
		String userId = request.getParameter("id");
        Action action = Action.fromString(actionParam);

		switch (action) {
            case SAVE_USER:
                User user = null;
                if (userId != null && userId.equals("New")) {
					user = new User();
				} else {
					Optional<User> userOpt = UsersService.findById(Long.parseLong(userId));
					if (!userOpt.isPresent()) {
                        return;
                    }
                    user = userOpt.get();
				}

                user = saveUser(request, user);
				userId = String.valueOf(user.getId());
				break;
            case DEL_USER:
                if (delUser(userId)) {
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/users");
				return;
            case DEL_USER_SESSIONS:
                delUserSessions(userId);
				break;
		}

		response.sendRedirect(request.getContextPath() + "/users?id=" + String.valueOf(userId));
	}

    private boolean delUser(@NotNull String userId) {
        Optional<User> userOpt = UsersService.findById(Long.parseLong(userId));
        if (!userOpt.isPresent()) {
            return false;
        }
        UsersService.remove(userOpt.get());

        return true;
    }

    private void delUserSessions(@NotNull String userId) {
        User user = new User();
        user.setId(Long.parseLong(userId));
        EventsService.removeAll(user);
    }

    private User saveUser(@NotNull HttpServletRequest request, @NotNull User user) {
        user.setName(request.getParameter("firstName") + " " + request.getParameter("surname"));
        user.setLogin(request.getParameter("login"));
        user.setPassword(request.getParameter("password"));
        user.setUserType(User.Type.fromInt(Integer.parseInt(request.getParameter("userType"))));
        user = UsersService.save(user);

        return user;
    }

}
