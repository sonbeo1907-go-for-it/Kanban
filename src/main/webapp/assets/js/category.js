function prepareDelete(id, name, contextPath) {
    document.getElementById('deleteCategoryName').innerText = name;
    
    const deleteUrl = contextPath + '/admin-category?action=delete&id=' + id;
    document.getElementById('confirmDeleteBtn').setAttribute('href', deleteUrl);
}

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('msg');
    
    if (msg) {
        const toastEl = document.getElementById('actionToast');
        const toastMessage = document.getElementById('toastMessage');
        
        // Reset classes cũ
        toastEl.classList.remove('bg-success', 'bg-danger');

        if (msg === 'addSuccess') {
            toastMessage.innerText = "Đã thêm danh mục mới thành công!";
            toastEl.classList.add('bg-success');
        } else if (msg === 'deleteSuccess') {
            toastMessage.innerText = "Đã xóa danh mục thành công!";
            toastEl.classList.add('bg-success');
        } else if (msg === 'duplicate') {
            toastMessage.innerText = "Lỗi: Tên danh mục này đã tồn tại!";
            toastEl.classList.add('bg-danger');
        }
        
        if (window.bootstrap && window.bootstrap.Toast) {
            const bsToast = new bootstrap.Toast(toastEl);
            bsToast.show();
        }
    }
});