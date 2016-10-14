package com.m1namoto.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectResult extends ActionResult {
    
    public RedirectResult(String destination) {
        super(destination);
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(this.getDestination());
    }

}