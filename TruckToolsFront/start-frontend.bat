@echo off
chcp 65001 >nul
title TruckTools Frontend

echo ========================================
echo   TruckTools Frontend Service
echo ========================================
echo.

:: Check Node.js
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found. Please install Node.js
    pause
    exit /b 1
)
echo [OK] Node.js detected
echo.

cd /d %~dp0

:: Check and install dependencies
if not exist "node_modules" (
    echo [INSTALL] First run, installing dependencies...
    echo.
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] Dependencies installation failed
        pause
        exit /b 1
    )
    echo.
    echo [SUCCESS] Dependencies installed
    echo.
)

echo ========================================
echo [STARTING] Frontend Dev Server...
echo [URL] http://localhost:5173
echo.
echo Press Ctrl+C to stop
echo ========================================
echo.

:: Start Vite dev server
call npm run dev

echo.
pause

