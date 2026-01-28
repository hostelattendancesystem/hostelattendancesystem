@echo off
REM ========================================
REM Docker Cleanup Script
REM ========================================
REM This script stops and removes the test container

echo Stopping and removing test container...

docker stop attendance-automation-test 2>nul
docker rm attendance-automation-test 2>nul

echo.
echo Cleanup complete!
echo.

pause
