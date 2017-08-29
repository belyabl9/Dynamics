package com.m1namoto.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectResult extends ActionResult {
    
    public RedirectResult(String destination) {
        super(destination);
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(this.getDestination());
    }

}