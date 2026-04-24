@echo off
set "TOMCAT_WEBAPPS=D:\apache-tomcat-10.1.54\webapps"
set "WAR_NAME=kanban.war"

echo =========================================
echo 1. Dang lam sach va dong goi project...
echo =========================================
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build that bai! Vui long kiem tra lai code.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =========================================
echo 2. Dang xoa ban build cu trong Tomcat...
echo =========================================
if exist "%TOMCAT_WEBAPPS%\%WAR_NAME%" del /f /q "%TOMCAT_WEBAPPS%\%WAR_NAME%"
if exist "%TOMCAT_WEBAPPS%\kanban" rd /s /q "%TOMCAT_WEBAPPS%\kanban"

echo.
echo =========================================
echo 3. Dang copy file WAR moi vao Tomcat...
echo =========================================
copy "target\%WAR_NAME%" "%TOMCAT_WEBAPPS%\"

echo.
echo =========================================
echo THANH CONG! 
echo URL: http://localhost:8080/kanban/user-auth?action=login-page
echo =========================================
pause