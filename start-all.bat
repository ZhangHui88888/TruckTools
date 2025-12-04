@echo off
chcp 65001 >nul
title TruckTools Launcher

echo ========================================
echo   TruckTools One-Click Launcher
echo ========================================
echo.

:: Start Docker database if available
docker info >nul 2>&1
if %errorlevel% equ 0 (
    echo [DATABASE] Starting MySQL via Docker...
    docker-compose up -d mysql >nul 2>&1
    timeout /t 3 /nobreak >nul
    echo.
)

:: Start backend
echo [1/2] Starting Backend Service...
start "TruckTools-Backend" cmd /k "cd /d %~dp0TruckToolsBackend && call start-backend.bat"

:: Wait for backend startup
echo.
echo Waiting for backend to start (about 20s)...
timeout /t 20 /nobreak >nul

:: Start frontend
echo [2/2] Starting Frontend Service...
start "TruckTools-Frontend" cmd /k "cd /d %~dp0TruckToolsFront && call start-frontend.bat"

echo.
echo ========================================
echo   Services are starting...
echo   Backend: http://localhost:8080
echo   Frontend: http://localhost:5173
echo   API Docs: http://localhost:8080/doc.html
echo ========================================
echo.
echo TIP: Closing this window won't stop services
echo      Use stop-all.bat to stop all services
echo.
pause

