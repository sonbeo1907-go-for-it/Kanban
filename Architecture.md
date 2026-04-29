### Local Deployment.

```bash
docker-compose.yml
       │
       ├──> PostgreSQL Container (db) <─── Volume
       │            ↑
       │            │ JDBC (HikariCP)
       │            │
       └──> App Container (Tomcat) ───> đọc biến môi trường (DB_SERVER, DB_USER, DB_PASSWORD)
              │
              │
              └──> Browser (localhost:8080) → chạy ứng dụng
```

- Khi chạy docker-compose.yml, docker sẽ tạo ra 2 container nằm trên 1 mạng nội bộ ảo: PostgreSQL Container (chứa Postgres) và App Container (Chứa Server Tomcat).
- Một phân vùng bộ nhớ trên Docker (volume) sẽ được gán cho PostgreSQL Container -> Đảm bảo tính bên vững, cho phép database vẫn còn dữ liệu khi mà container bị tắt.
- App Container chứa file WAR sau khi build app và Tomcat, sẽ đọc các biến môi trường (DB_SERVER, DB_USER, DB_PASSWORD) để kết nối với PostgreSQL Container qua JDBC (HikariCP).
- Ta có thể sự dụng truy cập app qua App Container qua Browser được ánh xạ vào port 8080 của App Container.

### Github Actionflow and CI.

