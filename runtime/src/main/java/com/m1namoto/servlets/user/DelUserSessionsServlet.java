package com.m1namoto.servlets.user;

import com.m1namoto.domain.User;
import com.m1namoto.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for removal sessions of user
 */
@WebServlet("/user/delsessions")
public class DelUserSessionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DelUserSessionsServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("id");
        long userIdNum = Utils.validateNumericId(userId);
        delUserSessions(userIdNum);
        response.sendRedirect(request.getContextPath() + "/users?id=" + userId);
    }

    private void delUserSessions(long userId) {
        User user = new User();
        user.setId(userId);

        // TODO add code to delete user sessions
    }

}
