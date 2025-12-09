@echo off
chcp 65001 >nul
title TruckTools Launcher (Clean Build + Dev)

echo ========================================
echo   TruckTools Clean Build + Start (Dev)
echo ========================================
echo.

:: Step 0: Maven clean install
echo [0/3] Running Maven Clean Install...
cd /d %~dp0TruckToolsBackend
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Maven build failed!
    pause
    exit /b 1
)
echo [OK] Maven build completed successfully.
echo.

cd /d %~dp0

:: Start Docker database if available
docker info >nul 2>&1
if %errorlevel% equ 0 (
    echo [DATABASE] Starting MySQL via Docker...
    docker-compose up -d mysql >nul 2>&1
    timeout /t 3 /nobreak >nul
    echo.
)

:: Start backend with DevTools (hot reload)
echo [1/2] Starting Backend Service (DevTools Mode)...
start "TruckTools-Backend-Dev" cmd /k "cd /d %~dp0TruckToolsBackend && call start-backend-dev.bat"

:: Wait for backend startup
echo.
echo Waiting for backend to start (about 5s)...
timeout /t 5 /nobreak >nul

:: Start frontend
echo [2/2] Starting Frontend Service...
start "TruckTools-Frontend" cmd /k "cd /d %~dp0TruckToolsFront && call start-frontend.bat"

echo.
echo ========================================
echo   Services are starting (Dev Mode)
echo   Backend: http://localhost:8080
echo   Frontend: http://localhost:5173
echo   API Docs: http://localhost:8080/doc.html
echo.
echo   HOT RELOAD: mvn compile to trigger restart
echo ========================================
echo.
echo TIP: Closing this window won't stop services
echo      Use stop-all.bat to stop all services
echo.
pause
