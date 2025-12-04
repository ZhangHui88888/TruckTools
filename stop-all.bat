@echo off
chcp 65001 >nul
title TruckTools Stop Services

echo ========================================
echo   TruckTools Stop All Services
echo ========================================
echo.

:: Stop backend Java process
echo [1/2] Stopping Backend Service...
for /f "tokens=1" %%i in ('jps -l 2^>nul ^| findstr "truck-tools-admin"') do (
    taskkill /F /PID %%i >nul 2>&1
    echo       Stopped process PID: %%i
)

:: Stop frontend Node process
echo [2/2] Stopping Frontend Service...
for /f "tokens=2" %%i in ('netstat -ano 2^>nul ^| findstr ":5173"') do (
    taskkill /F /PID %%i >nul 2>&1
    echo       Stopped process PID: %%i
)

:: 关闭相关命令窗口
taskkill /F /FI "WINDOWTITLE eq TruckTools-Backend*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq TruckTools-Frontend*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq TruckTools - 后端服务*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq TruckTools - 前端服务*" >nul 2>&1

echo.
echo [TIP] Database keeps running (Stop with: docker-compose down)
echo.
echo ========================================
echo   All services stopped
echo ========================================
echo.
pause

