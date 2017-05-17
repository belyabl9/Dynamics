package com.m1namoto.servlets.user;

import com.m1namoto.domain.User;
import com.m1namoto.service.EventService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user/delSessions")
public class DelUserSessionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String ID_MUST_BE_SPECIFIED = "User id must be specified.";
    private static final String ONLY_DIGITS_IN_ID = "User id must contain only digits.";

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
        long userIdNum = validateId(userId);
        delUserSessions(userIdNum);
        response.sendRedirect(request.getContextPath() + "/users?id=" + userId);
    }

    private long validateId(@NotNull String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException(ID_MUST_BE_SPECIFIED);
        }

        long userIdNum;
        try {
            userIdNum = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ONLY_DIGITS_IN_ID);
        }

        return userIdNum;
    }

    private void delUserSessions(long userId) {
        User user = new User();
        user.setId(userId);
        EventService.removeAll(user);
    }

}
