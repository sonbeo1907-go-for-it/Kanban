    function openUpdateModal(id, title, desc, status, catId) {
        document.getElementById('upd-id').value = id;
        document.getElementById('upd-title').value = title;
        document.getElementById('upd-desc').value = desc;
        document.getElementById('upd-status').value = status;
        document.getElementById('upd-cat').value = catId;
        new bootstrap.Modal(document.getElementById('updateTaskModal')).show();
    }

    function confirmDelete(id) {
        document.getElementById('deleteTaskId').value = id;

        const modalElement = document.getElementById('deleteConfirmModal');
        let modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
        modal.show();
    }


    document.addEventListener('DOMContentLoaded', function () {
        const urlParams = new URLSearchParams(window.location.search);
        const msg = urlParams.get('msg');
        
        if (msg) {
            const toastEl = document.getElementById('actionToast');
            const toastMsg = document.getElementById('toastMessage');
            
            if (toastEl && toastMsg) {
                toastEl.classList.remove('bg-success', 'bg-danger', 'bg-info');

                if (msg === 'addSuccess') {
                    toastMsg.innerText = "Thêm task thành công!";
                    toastEl.classList.add('bg-success');
                } else if (msg === 'updateStatusSuccess') {
                    toastMsg.innerText = "Cập nhật trạng thái thành công!";
                    toastEl.classList.add('bg-info');
                } else if (msg === 'updateSuccess') {
                    toastMsg.innerText = "Đã cập nhật công việc!";
                    toastEl.classList.add('bg-success');
                } else if (msg === 'deleteSuccess') {
                    toastMsg.innerText = "Đã xóa task!";
                    toastEl.classList.add('bg-danger');
                }

                const toast = new bootstrap.Toast(toastEl);
                toast.show();
                
                window.history.replaceState({}, document.title, window.location.pathname);
            }
        }
    });



    function allowDrop(ev) {
        ev.preventDefault();
    }

    function drag(ev, taskId) {
        ev.dataTransfer.setData("taskId", taskId);
    }

    function drop(ev, newStatus) {
        ev.preventDefault();
        const taskId = ev.dataTransfer.getData("taskId");
        const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
        
        const url = `${contextPath}/user-dashboard?action=updateStatus&id=${taskId}&newStatus=${newStatus}`;

        fetch(url, { method: 'POST' })
        .then(response => {
            if (response.ok) {
                window.location.href = contextPath + "/user-dashboard?action=list&msg=updateStatusSuccess";
            } else {
                alert("Lỗi khi cập nhật trạng thái!");
            }
        })
        .catch(error => console.error('Error:', error));
    }