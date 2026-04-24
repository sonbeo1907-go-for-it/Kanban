package com.casestudy.kanban.filter;

import com.casestudy.kanban.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();

        String loginURI = contextPath + "/user-auth";
        boolean isStaticResource = requestURI.contains("/assets/");
        boolean isAuthAction = requestURI.startsWith(loginURI);
        boolean isLoginPage = requestURI.endsWith("login.jsp") || requestURI.endsWith("register.jsp");

        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        boolean isLoggedIn = (user != null);


        if (isLoggedIn || isStaticResource || isAuthAction || isLoginPage) {
            
            if (requestURI.contains("/admin-")) {
                if (user == null || user.getRoleId() != 1) {
                    httpResponse.sendRedirect(contextPath + "/user-dashboard?action=list");
                    return;
                }
            }
            
            if (isLoggedIn && (isLoginPage || (isAuthAction && requestURI.contains("login-page")))) {
                if (user.getRoleId() == 1) {
                    httpResponse.sendRedirect(contextPath + "/admin-category?action=list");
                } else {
                    httpResponse.sendRedirect(contextPath + "/user-dashboard?action=list");
                }
                return;
            }

            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(loginURI + "?action=login-page");
        }
    }
}