package com.m1namoto.servlets.user;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Const;
import com.m1namoto.utils.Utils;
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
 * Provides access to user list, creating a new user and editing of the existing one.
 *
 * If no parameters specified, redirect to user list page will be done
 * In order to redirect to user information page GET parameter 'id' must be specified.
 * If it equals to 'New', redirect to creating new user will be done.
 */
@WebServlet("/user")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("id");
		if (StringUtils.isNotEmpty(userId)) {
			if (!userId.equals("New")) {
                long userIdNum = Utils.validateNumericId(userId);
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

}
