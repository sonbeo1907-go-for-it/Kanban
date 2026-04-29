<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>My Kanban Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        .kanban-column { min-height: 70vh; border-radius: 8px; background-color: #ebedf0; padding: 10px; }
        .task-card { cursor: pointer; transition: transform 0.2s; border: none; }
        .task-card:hover { transform: translateY(-3px); }
        .category-label { font-size: 0.75rem; font-weight: bold; }
    </style>
</head>
<body class="bg-light">

<nav class="navbar navbar-dark bg-dark mb-4">
    <div class="container">
        <span class="navbar-brand">Kanban Manager</span>
        <div class="d-flex align-items-center">
            <span class="text-light me-3 small">Chào, ${currentUser.username}</span>
            <a href="<c:url value='/user-auth?action=logout' />" class="btn btn-outline-light btn-sm">Đăng xuất</a>
        </div>
    </div>
</nav>

<div class="container mb-4">
    <form action="<c:url value='/user-dashboard' />" method="get" class="row g-2 align-items-end">
        <input type="hidden" name="action" value="list">
        <div class="col-md-4">
            <label class="small fw-bold">Tìm kiếm</label>
            <input type="text" name="search" class="form-control form-control-sm" value="${searchKeyword}" placeholder="Tên công việc...">
        </div>
        <div class="col-md-3">
            <label class="small fw-bold">Danh mục</label>
            <select name="categoryId" class="form-select form-select-sm">
                <option value="0">Tất cả danh mục</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.id}" ${selectedCatId == cat.id ? 'selected' : ''}>${cat.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3 py-2">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" name="hideOldDone" value="true" id="hideOldDone" ${hideOldDone ? 'checked' : ''}>
                <label class="form-check-label small" for="hideOldDone">Ẩn Task hoàn thành > 7 ngày</label>
            </div>
        </div>
        <div class="col-md-2">
            <button type="submit" class="btn btn-dark btn-sm w-100">Lọc dữ liệu</button>
        </div>
    </form>
</div>

<div class="container">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="mb-0">Danh Sách Công Việc Của Tôi</h4>
        <button class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#addTaskModal">
            <i class="bi bi-plus-lg"></i> Thêm Task
        </button>
    </div>

    <div class="row g-3">
        <div class="col-md-4">
            <div class="kanban-column shadow-sm p-2" 
                 id="col-1" 
                 ondragover="allowDrop(event)" 
                 ondrop="drop(event, 1)">
                <h6 class="p-2 border-bottom fw-bold text-secondary text-uppercase">To Do</h6>
                <c:forEach var="t" items="${tasks}">
                    <c:if test="${t.status == 1}">
                        <div class="card task-card shadow-sm mb-2 p-3" 
                             id="task-${t.id}" 
                             draggable="true" 
                             ondragstart="drag(event, '${t.id}')">
                            <div class="d-flex justify-content-between align-items-start">
                                <span class="btn-sm p-0 text-danger border-0" style="cursor:pointer" onclick="confirmDelete('${t.id}')">
                                    <i class="bi bi-trash"></i>
                                </span>
                                <h6 class="mb-1 text-primary w-100 text-center" style="cursor:pointer"
                                    onclick="openUpdateModal('${t.id}', '${t.title}', '${t.description}', '${t.status}', '${t.category.id}')">
                                    ${t.title}
                                </h6>
                            </div>
                            <div class="mt-2 d-flex justify-content-end">
                                <span class="badge bg-secondary category-label">${t.category.name}</span>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <div class="col-md-4">
            <div class="kanban-column shadow-sm p-2" 
                 id="col-2" 
                 ondragover="allowDrop(event)" 
                 ondrop="drop(event, 2)">
                <h6 class="p-2 border-bottom fw-bold text-info text-uppercase">In Progress</h6>
                <c:forEach var="t" items="${tasks}">
                    <c:if test="${t.status == 2}">
                        <div class="card task-card shadow-sm mb-2 p-3" 
                             id="task-${t.id}" 
                             draggable="true" 
                             ondragstart="drag(event, '${t.id}')">
                            <div class="d-flex justify-content-between align-items-start">
                                <span class="btn-sm p-0 text-danger border-0" style="cursor:pointer" onclick="confirmDelete('${t.id}')">
                                    <i class="bi bi-trash"></i>
                                </span>
                                <h6 class="mb-1 text-dark w-100 text-center" style="cursor:pointer"
                                    onclick="openUpdateModal('${t.id}', '${t.title}', '${t.description}', '${t.status}', '${t.category.id}')">
                                    ${t.title}
                                </h6>
                            </div>
                            <div class="mt-2 d-flex justify-content-end">
                                <span class="badge bg-secondary category-label">${t.category.name}</span>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <div class="col-md-4">
            <div class="kanban-column shadow-sm bg-success-subtle p-2" 
                 id="col-3" 
                 ondragover="allowDrop(event)" 
                 ondrop="drop(event, 3)">
                <h6 class="p-2 border-bottom fw-bold text-success text-uppercase">Done</h6>
                <c:forEach var="t" items="${tasks}">
                    <c:if test="${t.status == 3}">
                        <div class="card task-card shadow-sm mb-2 p-3 opacity-75" 
                             id="task-${t.id}" 
                             draggable="true" 
                             ondragstart="drag(event, '${t.id}')">
                            <div class="d-flex justify-content-between align-items-start">
                                <span class="btn-sm p-0 text-danger border-0" style="cursor:pointer" onclick="confirmDelete('${t.id}')">
                                    <i class="bi bi-trash"></i>
                                </span>
                                <h6 class="mb-1 text-decoration-line-through text-muted w-100 text-center" style="cursor:pointer">
                                    ${t.title}
                                </h6>
                            </div>
                            <div class="mt-2 d-flex justify-content-end">
                                <span class="badge bg-success category-label">${t.category.name}</span>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="addTaskModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="<c:url value='/user-dashboard?action=add' />" method="post">
            <div class="modal-content">
                <div class="modal-header"><h5>Tạo công việc mới</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                <div class="modal-body">
                    <div class="mb-3"><label>Tiêu đề</label><input type="text" name="title" class="form-control" required></div>
                    <div class="mb-3"><label>Mô tả</label><textarea name="description" class="form-control" rows="3"></textarea></div>
                    <div class="mb-3">
                        <label>Danh mục</label>
                        <select name="categoryId" class="form-select" required>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.id}">${cat.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer"><button type="submit" class="btn btn-primary">Lưu task</button></div>
            </div>
        </form>
    </div>
</div>

<div class="modal fade" id="updateTaskModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="<c:url value='/user-dashboard?action=update' />" method="post">
            <input type="hidden" name="id" id="upd-id">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white"><h5>Chỉnh sửa công việc</h5><button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                <div class="modal-body">
                    <div class="mb-3"><label>Tiêu đề</label><input type="text" name="title" id="upd-title" class="form-control" required></div>
                    <div class="mb-3"><label>Mô tả</label><textarea name="description" id="upd-desc" class="form-control" rows="3"></textarea></div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label>Trạng thái</label>
                            <select name="status" id="upd-status" class="form-select">
                                <option value="1">TO DO</option>
                                <option value="2">IN PROGRESS</option>
                                <option value="3">DONE</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label>Danh mục</label>
                            <select name="categoryId" id="upd-cat" class="form-select">
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}">${cat.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer"><button type="submit" class="btn btn-primary">Cập nhật</button></div>
            </div>
        </form>
    </div>
</div>

<div class="modal fade" id="deleteConfirmModal" tabindex="-1">
    <div class="modal-dialog">
        <form id="deleteForm" action="<c:url value='/user-dashboard' />" method="post">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" id="deleteTaskId">
            
            <div class="modal-content">
                <div class="modal-header bg-danger text-white"><h5>Xác nhận xóa task</h5></div>
                <div class="modal-body">Hành động này sẽ ẩn task này khỏi dashboard.</div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Xóa ngay</button>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
    <div id="actionToast" class="toast align-items-center text-white bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body"><i class="bi bi-check-circle-fill me-2"></i><span id="toastMessage"></span></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/assets/js/task.js' />"></script>
</body>
</html>