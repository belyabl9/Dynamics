package com.m1namoto.servlets.user;

import com.google.common.base.Optional;
import com.m1namoto.domain.User;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for removal of user
 */
@WebServlet("/user/del")
public class DelUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        long userIdNum = Utils.validateNumericId(userId);
        Optional<User> userOpt = UserService.findById(userIdNum);
        if (!userOpt.isPresent()) {
            throw new ServletException("User with specified id must exists: " + userId);
        }
        UserService.remove(userOpt.get());
        response.sendRedirect(request.getContextPath() + "/users");
    }

}
