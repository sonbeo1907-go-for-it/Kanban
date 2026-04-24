package com.casestudy.kanban.controller;

import java.io.IOException;
import com.casestudy.kanban.dao.UserDAO;
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
    
    private IUserService userService;

    @Override
    public void init() throws ServletException {
        UserDAO userDAO = new UserDAO();
        userService = new UserServiceImpl(userDAO);
    }

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
                request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
                break;
            case "register-page":
                request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
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

        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            request.setAttribute("error", "Tên đăng nhập và mật khẩu không được để trống!");
            request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
            return;
        }

        try {
            User account = userService.login(user.trim(), pass);
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
                request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            request.setAttribute("error", "Hệ thống đang gặp sự cố, vui lòng thử lại sau!");
            request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");
        String rePass = request.getParameter("repassword");
        String fullName = request.getParameter("fullName");

        // Kiểm tra nhập liệu cơ bản
        if (user == null || user.trim().isEmpty()) {
            request.setAttribute("error", "Tên đăng nhập không được để trống!");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }
        if (pass == null || pass.trim().isEmpty()) {
            request.setAttribute("error", "Mật khẩu không được để trống!");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }
        if (pass.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }
        if (!pass.equals(rePass)) {
            request.setAttribute("error", "Mật khẩu nhập lại không khớp!");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            return;
        }

        try {
            boolean isSuccess = userService.register(user.trim(), pass, fullName != null ? fullName.trim() : "");
            if (isSuccess) {
                request.setAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
                request.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Tài khoản đã tồn tại!");
                request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
            }
        } catch (Exception e) {
            // Lỗi hệ thống
            e.printStackTrace();
            request.setAttribute("error", "Đăng ký thất bại do lỗi hệ thống, vui lòng thử lại sau!");
            request.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(request, response);
        }
    }
}