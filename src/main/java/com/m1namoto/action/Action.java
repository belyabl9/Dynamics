package com.m1namoto.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.m1namoto.page.PageData;

public abstract class Action {

    protected boolean isError;
    protected Map<String, String[]> requestParameters;
    
    protected abstract ActionResult execute() throws Exception;
    
    public void init(HttpServletRequest req) {
        requestParameters = req.getParameterMap();
    }
    
    private String getValueFromParamMap(Map<String, String[]> paramMap, String key) {
        String[] values = paramMap.get(key);
        return values == null ? null : values[0];
    }
    
    public String getRequestParamValue(String paramName) {
        return getValueFromParamMap(requestParameters, paramName);
    }
    
    public ActionResult executeAndPostProcess() {
        //if (!isValidUser()) {
        //    return createRedirectResult(getAuthenticationRedirectUrl());
        //}
        
        // get the result from the child class.
        ActionResult response = null;
        try {
            response = execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set error flag of the result
        response.setError(isError);
        
        return response;
    }
    
    /**
     * Generates a {@link ShowPageResult} with the information in this object.
     */
    public ShowPageResult createShowPageResult(String destination, PageData pageData) {
        return new ShowPageResult(destination, pageData);
    }
    
    public RedirectResult createRedirectResult(String destination) {
        return new RedirectResult(destination);
    }

    public AjaxResult createAjaxResult(PageData pageData) {
        return new AjaxResult(pageData);
    }
    
}
