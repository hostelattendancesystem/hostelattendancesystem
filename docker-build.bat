@echo off
REM ========================================
REM Docker Build and Test Script
REM ========================================
REM This script helps you test the Docker build locally before deploying to Render

echo ========================================
echo Attendance Automation - Docker Build
echo ========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [1/4] Cleaning previous builds...
docker rmi attendance-automation:latest 2>nul
echo.

echo [2/4] Building Docker image...
echo This may take 5-10 minutes on first build...
docker build -t attendance-automation:latest .
if %errorlevel% neq 0 (
    echo ERROR: Docker build failed!
    pause
    exit /b 1
)
echo.

echo [3/4] Docker image built successfully!
echo.

echo [4/4] Testing Docker container...
echo Starting container on port 8081...
echo.

REM Run the container with environment variables
REM NOTE: Replace [DB_PASSWORD] and [MAIL_PASSWORD] with actual values from application.properties
docker run -d ^
    --name attendance-automation-test ^
    -p 8081:8081 ^
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://pg-hostelattendancesystem-hostelattendancesystem-1073.h.aivencloud.com:27872/defaultdb?sslmode=require ^
    -e SPRING_DATASOURCE_USERNAME=avnadmin ^
    -e SPRING_DATASOURCE_PASSWORD=[DB_PASSWORD] ^
    -e SPRING_MAIL_HOST=smtp.gmail.com ^
    -e SPRING_MAIL_PORT=587 ^
    -e SPRING_MAIL_USERNAME=hostelattendancesystem@gmail.com ^
    -e SPRING_MAIL_PASSWORD="[MAIL_PASSWORD]" ^
    -e SPRING_PROFILES_ACTIVE=production ^
    attendance-automation:latest

if %errorlevel% neq 0 (
    echo ERROR: Failed to start container!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Container started successfully!
echo ========================================
echo.
echo Application URL: http://localhost:8081
echo Health Check: http://localhost:8081/actuator/health
echo.
echo Waiting 30 seconds for application to start...
timeout /t 30 /nobreak >nul
echo.

echo Testing health endpoint...
curl -s http://localhost:8081/actuator/health
echo.
echo.

echo ========================================
echo Useful Commands:
echo ========================================
echo View logs:     docker logs -f attendance-automation-test
echo Stop container: docker stop attendance-automation-test
echo Remove container: docker rm attendance-automation-test
echo ========================================
echo.

pause
