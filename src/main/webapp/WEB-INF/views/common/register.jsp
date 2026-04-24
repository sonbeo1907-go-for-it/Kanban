<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Đăng ký thành viên - Kanban</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<c:url value='/assets/css/auth-style.css' />" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card auth-card shadow p-4">
                <h3 class="text-center mb-4 text-success">TẠO TÀI KHOẢN</h3>
                
                <%-- Hiển thị thông báo lỗi --%>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show p-2 small" role="alert">
                        ${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" style="padding: 0.5rem;"></button>
                    </div>
                </c:if>

                <form action="<c:url value='/user-auth?action=register' />" method="post">
                    <div class="mb-3">
                        <label class="form-label font-weight-bold">Họ và tên</label>
                        <input type="text" name="fullName" class="form-control" placeholder="Nhập họ và tên" required>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label font-weight-bold">Tài khoản (Username)</label>
                        <input type="text" name="username" class="form-control" placeholder="Nhập tên đăng nhập" required>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label font-weight-bold">Mật khẩu</label>
                            <input type="password" name="password" class="form-control" placeholder="********" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label font-weight-bold">Xác nhận</label>
                            <input type="password" name="repassword" class="form-control" placeholder="********" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-success w-100 mb-3 py-2 shadow-sm">
                        Đăng ký ngay
                    </button>
                    
                    <div class="text-center border-top pt-3">
                        <span class="text-muted">Đã có tài khoản? </span>
                        <a href="<c:url value='/user-auth?action=login-page' />" class="text-decoration-none fw-bold">
                            Quay lại Đăng nhập
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/assets/js/main.js' />"></script>
</body>
</html>