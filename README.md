# Kanban Task Manager

Ứng dụng quản lý công việc theo phương pháp Kanban (To Do / In Progress / Done) dành cho cá nhân và nhóm nhỏ.  
Xây dựng bằng Java thuần (Servlets + JSP), kết nối PostgreSQL, chạy trên Tomcat, đóng gói Docker.

![Java Version](https://img.shields.io/badge/Java-17-blue)
![Docker](https://img.shields.io/badge/Docker-✔-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## Tính năng chính

- 🔐 Đăng ký / đăng nhập tài khoản (mật khẩu được mã hoá BCrypt)
- 👥 Phân quyền: **USER** (bảng Kanban cá nhân) và **ADMIN** (quản lý danh mục)
- 📝 Quản lý task: thêm, sửa, xoá mềm, **kéo thả** thay đổi trạng thái (To Do, In Progress, Done)
- 🔍 Lọc task theo danh mục, từ khoá, ẩn task cũ hoàn thành >7 ngày
- 📱 Giao diện đáp ứng (Bootstrap 5)

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|------------|
| Backend | Java 17, Jakarta Servlet 6.0, JSP + JSTL |
| Database | PostgreSQL 15 |
| Connection Pool | HikariCP |
| Security | BCrypt, Docker secrets |
| Testing | JUnit 5, Testcontainers, Mockito |
| Build & Deploy | Maven, Docker, GitHub Actions, Render |

## Yêu cầu hệ thống

- Docker & Docker Compose (khuyến nghị)
- Hoặc JDK 17 + Maven + Tomcat 10 (nếu chạy thủ công)

## Cài đặt và chạy bằng Docker (khuyến nghị)

### 1. Clone repository

```bash
git clone https://github.com/sonbeo1907-go-for-it/Kanban.git
cd Kanban

