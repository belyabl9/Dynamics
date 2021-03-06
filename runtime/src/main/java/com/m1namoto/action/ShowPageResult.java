package com.m1namoto.action;

import com.m1namoto.page.PageData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ShowPageResult extends ActionResult {
    public PageData data;
    
    public ShowPageResult(String destination, PageData data) {
        super(destination);
        this.data = data;
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setAttribute("data", data);
        req.setAttribute("error", Boolean.toString(isError()));
        
        req.getRequestDispatcher(getDestination()).forward(req, resp);
    }
}
