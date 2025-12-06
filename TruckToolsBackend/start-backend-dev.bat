@echo off
chcp 65001 >nul
title TruckTools Backend (DevTools)

echo ========================================
echo   TruckTools Backend (DevTools Mode)
echo ========================================
echo.

cd /d %~dp0

:: Check Java
echo [CHECK] Java Environment...
java -version 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 17+
    pause
    exit /b 1
)
echo.

echo ========================================
echo [STARTING] Backend with DevTools...
echo [URL] http://localhost:8080
echo [API] http://localhost:8080/doc.html
echo.
echo HOT RELOAD: Save file to trigger restart
echo Press Ctrl+C to stop
echo ========================================
echo.

:: Run with DevTools (auto compile included)
mvn spring-boot:run -pl truck-tools-admin -Dspring-boot.run.profiles=dev

pause
