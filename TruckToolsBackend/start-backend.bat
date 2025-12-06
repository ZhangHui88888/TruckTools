@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title TruckTools Backend

echo ========================================
echo   TruckTools Backend Service
echo ========================================
echo.

cd /d %~dp0

:: Check Java
echo [CHECK] Java Environment...
java -version 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Java not found. Please install JDK 17+
    echo.
    pause
    exit /b 1
)
echo [OK] Java detected
echo.

:: Check Maven
echo [CHECK] Maven Environment...
call mvn -version
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Maven not found. Please install Maven
    echo.
    pause
    exit /b 1
)
echo [OK] Maven detected
echo.

:: Build project
echo [BUILD] Compiling project...
echo.
call mvn package -DskipTests
if !errorlevel! neq 0 (
    echo.
    echo [ERROR] Build failed. Check error messages above.
    echo.
    pause
    exit /b 1
)
if not exist "truck-tools-admin\target\truck-tools-admin-1.0.0.jar" (
    echo.
    echo [ERROR] Build failed. JAR file not generated.
    echo.
    pause
    exit /b 1
)
echo.
echo [SUCCESS] Build completed
echo.

:: Check environment
echo [CHECK] MySQL Connection (Port 3306)...
netstat -ano | findstr ":3306" >nul
if %errorlevel% neq 0 (
    echo [WARN] MySQL not detected
) else (
    echo [OK] MySQL is running
)

echo [CHECK] Port 8080 Availability...
netstat -ano | findstr ":8080.*LISTENING" >nul
if %errorlevel% equ 0 (
    echo [WARN] Port 8080 is in use
) else (
    echo [OK] Port 8080 is available
)
echo.

echo ========================================
echo [STARTING] Backend Service...
echo [URL] http://localhost:8080
echo [API] http://localhost:8080/doc.html
echo.
echo Press Ctrl+C to stop
echo ========================================
echo.

:: Start with dev profile
java -jar truck-tools-admin\target\truck-tools-admin-1.0.0.jar --spring.profiles.active=dev

echo.
echo ========================================
echo Service stopped. Press any key to exit.
echo ========================================
pause

