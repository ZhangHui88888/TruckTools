@echo off
chcp 65001 >nul
title TruckTools - 项目构建

echo ========================================
echo     TruckTools 项目构建脚本
echo ========================================
echo.

:: 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)

:: 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Maven环境，请先安装Maven
    pause
    exit /b 1
)

:: 检查Node环境
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请先安装Node.js
    pause
    exit /b 1
)

echo [1/2] 正在构建后端项目...
echo.
cd /d %~dp0TruckToolsBackend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 后端构建失败
    pause
    exit /b 1
)
echo.
echo [成功] 后端构建完成
echo.

echo [2/2] 正在构建前端项目...
echo.
cd /d %~dp0TruckToolsFront
if not exist "node_modules" (
    call npm install
)
call npm run build
if %errorlevel% neq 0 (
    echo [错误] 前端构建失败
    pause
    exit /b 1
)
echo.
echo [成功] 前端构建完成
echo.

echo ========================================
echo   所有项目构建完成！
echo   
echo   后端JAR: TruckToolsBackend\truck-tools-admin\target\truck-tools-admin-1.0.0.jar
echo   前端产物: TruckToolsFront\dist\
echo ========================================
echo.
pause

