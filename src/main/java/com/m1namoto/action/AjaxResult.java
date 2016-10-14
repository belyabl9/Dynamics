package com.m1namoto.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.m1namoto.page.PageData;

public class AjaxResult extends ActionResult {

    public PageData data;
    public boolean isClearingStatusMessage = true;
    
    public AjaxResult(String destination) {
        super(destination);
    }

    public AjaxResult(PageData data) {
        super("");
        this.data = data;
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setAttribute("error", Boolean.toString(isError()));

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String jsonData = new Gson().toJson(data);
        
        resp.getWriter().write(jsonData);
    }

}