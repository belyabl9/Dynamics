package com.m1namoto.servlets;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.UsersService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user/del")
public class DelUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String ID_MUST_BE_SPECIFIED = "User id must be specified.";
    private static final String ONLY_DIGITS_IN_ID = "User id must contain only digits.";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DelUserServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("id");
        long userIdNum = validateId(userId);
        Optional<User> userOpt = UsersService.findById(userIdNum);
        if (!userOpt.isPresent()) {
            throw new ServletException("User with specified id must exists: " + userId);
        }
        delUser(userId);
        response.sendRedirect(request.getContextPath() + "/users");
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

    private boolean delUser(@NotNull String userId) {
        Optional<User> userOpt = UsersService.findById(Long.parseLong(userId));
        if (!userOpt.isPresent()) {
            return false;
        }
        UsersService.remove(userOpt.get());

        return true;
    }

}
