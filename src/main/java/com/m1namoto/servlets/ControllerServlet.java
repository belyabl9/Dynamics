package com.m1namoto.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.m1namoto.action.Action;
import com.m1namoto.action.ActionFactory;
import com.m1namoto.action.ActionResult;

/**
 * Servlet implementation class ControllerServlet
 */
public class ControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger logger = Logger.getLogger(ControllerServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ControllerServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Processing request: " + request.getRequestURI());
        long startTime = System.currentTimeMillis();
        
        Action c;
        try {
            c = new ActionFactory().getAction(request);
            ActionResult actionResult = c.executeAndPostProcess();
            actionResult.send(request, response);
        } catch (Exception e) {
            logger.error("Can not execute action", e);
        }

        long timeTaken = System.currentTimeMillis() - startTime;
        logger.info("Request processing time:" + timeTaken);
    }

}
