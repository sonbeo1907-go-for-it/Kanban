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
```

### 2. Tạo file secret cho database
Tạo hai file trong cùng thư mục với docker-compose.yml:
- db_root_password.txt – chứa mật khẩu root của PostgreSQL
- db_app_password.txt – chứa mật khẩu của user kanban_user

### 3. Cấu hình biến môi trường
Copy .env.example thành .env (nếu chưa có) hoặc sửa trực tiếp.

### 4. Khởi động
```bash
docker-compose up -d
```
Lần đầu chạy sẽ:
- Pull image PostgreSQL
- Khởi tạo database với schema và dữ liệu mẫu (categories, roles)
- Tự động tạo user kanban_user từ secret
- Deploy ứng dụng Tomcat

### 5. Truy cập
- Ứng dụng: http://localhost:8080/user-auth?action=login-page
- Tài khoản mặc định: admin / admin (được tạo tự động nếu chưa có user nào)

### 6. Dừng và xóa dữ liệu
```bash
docker-compose down           # dừng container, giữ dữ liệu
docker-compose down -v        # dừng và xóa volume (mất toàn bộ dữ liệu)
```

## Chạy bằng Maven & Tomcat (thủ công)
```bash
mvn clean package
cp target/kanban.war $TOMCAT_HOME/webapps/ROOT.war
$TOMCAT_HOME/bin/catalina.sh run
```
## Cấu Trúc Thư Mục
```bash
Kanban/
├── src/
│   ├── main/
│   │   ├── java/com/casestudy/kanban/
│   │   │   ├── controller/   # Servlets
│   │   │   ├── dao/          # Truy vấn database
│   │   │   ├── filter/       # AuthenticationFilter
│   │   │   ├── listener/     # (Deprecated) DatabaseInitializer
│   │   │   ├── model/        # POJO
│   │   │   ├── service/      # Business logic
│   │   │   └── util/         # DBContext (HikariCP)
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   │   ├── views/    # JSP (được bảo vệ)
│   │   │   │   └── web.xml
│   │   │   └── assets/       # CSS, JS, ảnh
│   │   └── resources/        # Cấu hình khởi tạo 1 Stub Database.
│   └── test/                 # Unit & integration tests
├── .env.example
├── docker-compose.yml
├── Dockerfile
├── init.sql
├── init_postgres.sql
├── pom.xml
└── README.md
```

## Chạy Test
```bash
mvn clean test
```
Các test sử dụng Testcontainers, yêu cầu Docker đang chạy.
Test sẽ tự động tạo container PostgreSQL tạm thời, chạy script init_test.sql, và dọn dẹp sau khi kết thúc.

## Live Render
Ứng Dụng Live trên Render: https://kanban-app-latest.onrender.com/
***Lưu ý:*** Phiên bản Live trên Render chỉ có thời hạn đến hết tháng 5/2026.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

