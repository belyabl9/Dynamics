package com.m1namoto.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class Auth implements Filter {

	private static String[] PAGES_WITHOUT_REG = {
		"/login", "/auth", "/reg", "/dynamics.jsp"
	};

    /**
     * Default constructor.
     */
    public Auth() {}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.println("Filter Called");

		HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
         
        String uri = req.getRequestURI();
         
        HttpSession session = req.getSession(false);
        
        if(session == null && !(uri.endsWith("login")) && !(uri.endsWith("saveSession")) 
                && !(uri.endsWith("auth")) && !(uri.endsWith("reg")) && !(uri.endsWith(".css")) && !(uri.endsWith("dynamics.jsp")) && !(uri.endsWith("action/dbCleanup"))  ) {
        	System.out.println(uri);
        	res.sendRedirect("/login");
        } else {
        	if (session != null && uri.endsWith("login")) {
        		res.sendRedirect("/");
        		return;
        	}
            // pass the request along the filter chain
            chain.doFilter(request, response);
        }
	}

}
