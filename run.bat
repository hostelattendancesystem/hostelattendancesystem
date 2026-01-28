@echo off
echo ========================================
echo College Attendance Automation System
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    pause
    exit /b 1
)

echo [1/3] Checking Aiven PostgreSQL connection...
echo.
echo Database: Aiven PostgreSQL (Free Forever - 1 GB)
echo Host: pg-hostelattendancesystem-hostelattendancesystem-1073.h.aivencloud.com
echo Port: 27872
echo.

echo [2/3] Building the application...
echo.
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting the application...
echo.
echo Backend will start on http://localhost:8081
echo Admin Dashboard: http://localhost:8081/admin-auth.html
echo Student Portal: http://localhost:8081/user-auth.html
echo.
echo Press Ctrl+C to stop the application
echo.
echo ========================================
echo.

call mvn spring-boot:run

pause
