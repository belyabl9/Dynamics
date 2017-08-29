package com.m1namoto.action;

import com.google.common.base.Optional;
import com.m1namoto.page.PageData;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class Action {
    private final static Logger log = Logger.getLogger(Action.class);

    private static final String PARAMETER_NAME_MUST_BE_SPECIFIED = "Parameter name must be specified.";
    private static final String KEY_MUST_BE_SPECIFIED = "Key must be specified.";
    private static final String DESTINATION_MUST_BE_SPECIFIED = "Destination must be specified.";

    protected boolean isError;
    protected Map<String, String[]> requestParameters;
    
    protected abstract ActionResult execute() throws Exception;
    
    public void init(@NotNull HttpServletRequest req) {
        requestParameters = req.getParameterMap();
    }

    @NotNull
    private Optional<String> getValueFromParamMap(@NotNull Map<String, String[]> paramMap, @NotNull String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException(KEY_MUST_BE_SPECIFIED);
        }
        String[] values = paramMap.get(key);
        return values != null && values.length > 0 ? Optional.of(values[0]) : Optional.<String>absent();
    }

    @NotNull
    public Optional<String> getRequestParamValue(@NotNull String paramName) {
        if (paramName.isEmpty()) {
            throw new IllegalArgumentException(PARAMETER_NAME_MUST_BE_SPECIFIED);
        }
        return getValueFromParamMap(requestParameters, paramName);
    }
    
    public ActionResult executeAndPostProcess() {
        ActionResult response;
        try {
            response = execute();
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        response.setError(isError);

        return response;
    }
    
    /**
     * Generates a {@link ShowPageResult} with the information in this object.
     */
    public ShowPageResult createShowPageResult(String destination, PageData pageData) {
        return new ShowPageResult(destination, pageData);
    }
    
    public RedirectResult createRedirectResult(@NotNull String destination) {
        if (destination.isEmpty()) {
            throw new IllegalArgumentException(DESTINATION_MUST_BE_SPECIFIED);
        }
        return new RedirectResult(destination);
    }

    public AjaxResult createAjaxResult(@NotNull PageData pageData) {
        return new AjaxResult(pageData);
    }
    
}
