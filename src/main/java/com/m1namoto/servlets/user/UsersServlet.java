package com.m1namoto.servlets.user;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class Users
 */
@WebServlet("/user")
public class UsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static final String ONLY_DIGITS_IN_ID = "User id must contain only digits.";

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
		if (StringUtils.isNotEmpty(userId)) {
			if (!userId.equals("New")) {
                long userIdNum = validateId(userId);
                Optional<User> userOpt = UserService.findById(userIdNum);
				if (userOpt.isPresent()) {
					request.setAttribute("user", userOpt.get());
					request.getRequestDispatcher(Const.ViewURIs.USER_INFO).forward(request, response);
					return;
				}
			}
			request.getRequestDispatcher(Const.ViewURIs.ADD_USER).forward(request, response);
			return;
		}

		List<User> users = UserService.getList();
		request.setAttribute("users", users);
		request.getRequestDispatcher(Const.ViewURIs.USERS).forward(request, response);
	}

    private long validateId(@NotNull String id) {
        long userIdNum;
        try {
            userIdNum = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ONLY_DIGITS_IN_ID);
        }

        return userIdNum;
    }

}
