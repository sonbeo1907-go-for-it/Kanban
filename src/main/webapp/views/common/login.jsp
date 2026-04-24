<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Đăng nhập - Kanban</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <%-- Dùng c:url để lấy đường dẫn file CSS --%>
    <link href="<c:url value='/assets/css/auth-style.css' />" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="card shadow p-4">
                <h3 class="text-center mb-4 text-primary">KANBAN LOGIN</h3>
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger p-2 small">${error}</div>
                </c:if>
                <c:if test="${not empty message}">
                    <div class="alert alert-success p-2 small">${message}</div>
                </c:if>

                <form action="<c:url value='/user-auth?action=login' />" method="post">
                    <div class="mb-3">
                        <label class="form-label">Tài khoản</label>
                        <input type="text" name="username" class="form-control" placeholder="Nhập username" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>
                        <input type="password" name="password" class="form-control" placeholder="********" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 mb-3">Đăng nhập</button>
                    <div class="text-center">
                        <span>Chưa có tài khoản? </span>
                        <a href="<c:url value='/user-auth?action=register-page' />" class="text-decoration-none">Đăng ký ngay</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script src="<c:url value='/assets/js/main.js' />"></script>
</body>
</html>