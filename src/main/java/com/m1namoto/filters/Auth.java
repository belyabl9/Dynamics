package com.m1namoto.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class Auth
 */
@WebFilter("/*")
public class Auth implements Filter {

	public static String[] PAGES_WITHOUT_REG = {
		"/login", "/login.jsp"
	};
	
    /**
     * Default constructor. 
     */
    public Auth() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

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
                && !(uri.endsWith("auth")) && !(uri.endsWith("reg")) && !(uri.endsWith(".css")) ) {
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

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
