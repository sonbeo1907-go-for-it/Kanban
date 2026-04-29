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

### Github Actionflow and CI/CD.

```bash
GitHub Repository
       │
       │ git push (main)
       ▼
GitHub Actions (Workflow)
       │
       ├──> Checkout code
       │
       ├──> Set up JDK 17
       │
       ├──> Run Maven clean verify
       │          │
       │          ├──> Unit tests (JUnit 5)
       │          │
       │          └──> Integration tests (Testcontainers)
       │                    │
       │                    └──> Temporary PostgreSQL container (chạy trong CI)
       │
       │ (tests passed)
       ▼
       ├──> Login to Docker Hub (using secrets)
       │
       ├──> Docker build & push
       │          │
       │          ├──> kanban-app:latest
       │          └──> kanban-app:commit-sha
       │
       ▼
       └──> Trigger Render Deploy (via webhook)
                      │
                      ▼
                Render Web Service
                      │
                      └──> Pull image:latest & redeploy
```

- Luồng CI sẽ được trigger bởi hành động push code lên nhánh main của repository.
- Khi CI được trigger, nó sẽ checkout code để bắt đầu thực hiện setup môi trường trong 1 runner ảo có sẵn Docker của GitHub.
- Maven sẽ thực hiện việc chạy các Unit Test (JUnit 5), và khi chạy Integration Test sẽ tạo 1 cái Testcontainer bên trong Runner của GitHub để chạy PostgresSQL -> tạo một Database THẬT để Test (gần giống production).
- Khi các bài test đã pass, cái Testcontainer chứa Postgres sẽ bị kill.
- Luồng CI sẽ đăng nhập vào DockerHub, sử dựng biến trong secrets của GitHub.
- Khi đăng nhập xong, nó sẽ build image mới và push lên repository trên DockerHub với tag latest.
- Khi mọi hành động đã xong, luồng CD sẽ hoạt động.
- Luồng CD sẽ dùng Curl để gửi 1 Post Request lên server của Render Deploy Hook mà App được deploy lên.
- Render sẽ tự động kéo latest image từ DockerHub để triển khai.