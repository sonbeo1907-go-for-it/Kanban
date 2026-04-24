<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Admin Dashboard - Categories</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body class="bg-light">

<nav class="navbar navbar-dark bg-dark mb-4">
    <div class="container">
        <span class="navbar-brand">Kanban Admin</span>
        <a href="<c:url value='/user-auth?action=logout' />" class="btn btn-outline-light btn-sm">Đăng xuất</a>
    </div>
</nav>

<div class="container mb-4">
    <div class="row">
        <div class="col-md-6 mx-auto">
            <form action="<c:url value='/admin-category' />" method="get" class="d-flex shadow-sm">
                <input type="hidden" name="action" value="list">
                <input type="text" name="search" class="form-control me-2" 
                       placeholder="Tìm kiếm danh mục..." value="${searchKeyword}">
                <button class="btn btn-dark" type="submit">
                    <i class="bi bi-search"></i>
                </button>
                <c:if test="${not empty searchKeyword}">
                    <a href="<c:url value='/admin-category?action=list' />" class="btn btn-outline-secondary ms-2">
                        <i class="bi bi-x-lg"></i>
                    </a>
                </c:if>
            </form>
        </div>
    </div>
</div>

<div class="container">
    <div class="card shadow-sm">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0">Quản lý Danh mục</h5>
            <button class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#addCategoryModal">
                <i class="bi bi-plus-lg"></i> Thêm mới
            </button>
        </div>
        
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th style="width: 10%">ID</th>
                        <th>Tên Danh mục</th>
                        <th style="width: 15%" class="text-center">Thao tác</th>
                    </tr>
                </thead>

                <c:if test="${empty categories}">
                    <tr>
                        <td colspan="3" class="text-center text-muted py-4">
                            Không tìm thấy danh mục nào phù hợp với từ khóa "${searchKeyword}"
                        </td>
                    </tr>
                </c:if>

                <tbody>
                    <c:forEach var="cat" items="${categories}">
                        <tr>
                            <td>${cat.id}</td>
                            <td><span class="badge bg-info text-dark">${cat.name}</span></td>
                            <td class="text-center">
                                <button type="button" 
                                        class="btn btn-outline-danger btn-sm" 
                                        data-bs-toggle="modal" 
                                        data-bs-target="#deleteConfirmModal"
                                        onclick="prepareDelete('${cat.id}', '${cat.name}', '${pageContext.request.contextPath}')">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <nav class="mt-4">
                <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                        <a class="page-link" href="<c:url value='/admin-category?action=list&page=${currentPage - 1}&search=${searchKeyword}' />">Trước</a>
                    </li>

                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <li class="page-item ${currentPage == i ? 'active' : ''}">
                            <a class="page-link" href="<c:url value='/admin-category?action=list&page=${i}&search=${searchKeyword}' />">${i}</a>
                        </li>
                    </c:forEach>

                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="<c:url value='/admin-category?action=list&page=${currentPage + 1}&search=${searchKeyword}' />">Sau</a>
                    </li>
                </ul>
            </nav>

        </div>
    </div>
</div>

<div class="modal fade" id="addCategoryModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="<c:url value='/admin-category?action=add' />" method="post">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Thêm Danh mục mới</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Tên danh mục</label>
                        <input type="text" name="categoryName" class="form-control" placeholder="Ví dụ: Backend, Frontend..." required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="submit" class="btn btn-primary">Lưu lại</button>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header bg-danger text-white">
                <h5 class="modal-title">Xác nhận xóa</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                Bạn có chắc chắn muốn xóa danh mục <strong id="deleteCategoryName"></strong>?
                <p class="text-muted small mt-2">Lưu ý: Hành động này không thể hoàn tác trực tiếp.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                <a id="confirmDeleteBtn" href="#" class="btn btn-danger">Xóa ngay</a>
            </div>
        </div>
    </div>
</div>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
    <div id="actionToast" class="toast align-items-center text-white bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body">
                <i class="bi bi-check-circle-fill me-2"></i>
                <span id="toastMessage">Thông báo ở đây</span>
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/assets/js/category.js' />"></script>
</body>
</html>