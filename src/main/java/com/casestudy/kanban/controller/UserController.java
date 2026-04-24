package com.casestudy.kanban.controller;

import java.io.IOException;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.service.IUserService;
import com.casestudy.kanban.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/user-auth", ""})
public class UserController extends HttpServlet {
    
    private IUserService userService = new UserServiceImpl();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String contextPath = request.getContextPath(); 
        
        if (action == null) {
            response.sendRedirect(contextPath + "/user-auth?action=login-page");
            return;
        }

        switch (action) {
            case "login-page":
                request.getRequestDispatcher("/views/common/login.jsp").forward(request, response);
                break;
            case "register-page":
                request.getRequestDispatcher("/views/common/register.jsp").forward(request, response);
                break;
            case "logout":
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(contextPath + "/user-auth?action=login-page&msg=logout");
                break;
            default:
                response.sendRedirect(contextPath + "/user-auth?action=login-page");
                break;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("register".equals(action)) {
            handleRegister(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        User account = userService.login(user, pass);
        if (account != null) {
            HttpSession session = request.getSession();
            session.setAttribute("currentUser", account);
            
            if (account.getRoleId() == 1) {
                response.sendRedirect(contextPath + "/admin-category?action=list");
            } else {
                response.sendRedirect(contextPath + "/user-dashboard?action=list");
            }
        } else {
            request.setAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            request.getRequestDispatcher("/views/common/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");
        String rePass = request.getParameter("repassword");
        String fullName = request.getParameter("fullName");

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin!");
            request.getRequestDispatcher("/views/common/register.jsp").forward(request, response);
            return;
        }

        if (!pass.equals(rePass)) {
            request.setAttribute("error", "Mật khẩu nhập lại không khớp!");
            request.getRequestDispatcher("/views/common/register.jsp").forward(request, response);
            return;
        }

        boolean isSuccess = userService.register(user, pass, fullName);

        if (isSuccess) {
            request.setAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            request.getRequestDispatcher("/views/common/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Tài khoản đã tồn tại hoặc có lỗi xảy ra!");
            request.getRequestDispatcher("/views/common/register.jsp").forward(request, response);
        }
    }
}