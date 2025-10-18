@echo off
chcp 65001 >nul
cls

echo ==========================================
echo    辽师大题库系统 - 一键部署脚本
echo ==========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose未安装
    pause
    exit /b 1
)

echo ✅ Docker环境检查通过
echo.

echo 请选择部署方式：
echo 1) 本地Docker部署（开发测试）
echo 2) 准备Railway部署
echo 3) 准备Render部署
echo 4) Docker镜像打包
echo.
set /p choice=请输入选项 (1-4): 

if "%choice%"=="1" goto local_deploy
if "%choice%"=="2" goto railway_deploy
if "%choice%"=="3" goto render_deploy
if "%choice%"=="4" goto docker_build
echo ❌ 无效选项
pause
exit /b 1

:local_deploy
echo.
echo 🚀 开始本地Docker部署...
echo.

echo 📦 停止旧容器...
docker-compose down

echo 🔨 构建Docker镜像...
docker-compose build

echo 🚀 启动服务...
docker-compose up -d

echo ⏳ 等待服务启动（30秒）...
timeout /t 30 /nobreak >nul

echo.
echo 📊 服务状态：
docker-compose ps

echo.
echo ✅ 部署完成！
echo.
echo 🌐 访问地址：
echo    前端: http://localhost:8080
echo    后端API: http://localhost:8080/api
echo.
echo 📝 查看日志: docker-compose logs -f
echo 🛑 停止服务: docker-compose down
goto end

:railway_deploy
echo.
echo 📦 准备Railway部署...
echo.

git rev-parse --git-dir >nul 2>&1
if %errorlevel% neq 0 (
    echo 初始化Git仓库...
    git init
    git add .
    git commit -m "Initial commit for Railway deployment"
)

echo ✅ Railway部署准备完成！
echo.
echo 📋 部署步骤：
echo 1. 访问 https://railway.app
echo 2. 点击 'New Project' -^> 'Deploy from GitHub repo'
echo 3. 选择此仓库
echo 4. 添加MySQL数据库服务
echo 5. 配置环境变量（Railway会自动识别）
echo 6. 部署完成！
goto end

:render_deploy
echo.
echo 📦 准备Render部署...
echo.
echo ✅ Render部署准备完成！
echo.
echo 📋 部署步骤：
echo 1. 访问 https://render.com
echo 2. 点击 'New' -^> 'Blueprint'
echo 3. 连接此仓库
echo 4. Render会自动读取 render.yaml
echo 5. 点击 'Apply' 开始部署
echo 6. 部署完成！
goto end

:docker_build
echo.
echo 🔨 打包Docker镜像...
echo.

set IMAGE_NAME=tiku-system
set IMAGE_TAG=latest

docker build -t %IMAGE_NAME%:%IMAGE_TAG% .

echo.
echo ✅ 镜像打包完成！
echo.
echo 📦 镜像信息：
docker images | findstr %IMAGE_NAME%
echo.
echo 🚀 运行镜像：
echo    docker run -p 8080:8080 %IMAGE_NAME%:%IMAGE_TAG%
goto end

:end
echo.
echo ==========================================
echo    部署流程完成！
echo ==========================================
echo.
pause

