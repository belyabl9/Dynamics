package com.m1namoto.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ActionResult {
    
    private String destination;
    private boolean isError;
    
    public ActionResult(String destination) {
        this.destination = destination;
    }
    
    /**
     * Sends the result to the intended URL.
     */
    public abstract void send(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException;
    
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

}
