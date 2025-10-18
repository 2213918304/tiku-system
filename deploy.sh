#!/bin/bash

echo "=========================================="
echo "   辽师大题库系统 - 一键部署脚本"
echo "=========================================="
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

echo "✅ Docker环境检查通过"
echo ""

# 提示用户选择部署方式
echo "请选择部署方式："
echo "1) 本地Docker部署（开发测试）"
echo "2) 生成Railway部署包"
echo "3) 生成Render部署包"
echo "4) Docker镜像打包"
read -p "请输入选项 (1-4): " choice

case $choice in
  1)
    echo ""
    echo "🚀 开始本地Docker部署..."
    echo ""
    
    # 停止旧容器
    echo "📦 停止旧容器..."
    docker-compose down
    
    # 构建镜像
    echo "🔨 构建Docker镜像..."
    docker-compose build
    
    # 启动服务
    echo "🚀 启动服务..."
    docker-compose up -d
    
    # 等待服务启动
    echo "⏳ 等待服务启动（30秒）..."
    sleep 30
    
    # 检查服务状态
    echo ""
    echo "📊 服务状态："
    docker-compose ps
    
    echo ""
    echo "✅ 部署完成！"
    echo ""
    echo "🌐 访问地址："
    echo "   前端: http://localhost:8080"
    echo "   后端API: http://localhost:8080/api"
    echo ""
    echo "📝 查看日志: docker-compose logs -f"
    echo "🛑 停止服务: docker-compose down"
    ;;
    
  2)
    echo ""
    echo "📦 准备Railway部署包..."
    
    # 检查Git
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo "初始化Git仓库..."
        git init
        git add .
        git commit -m "Initial commit for Railway deployment"
    fi
    
    echo ""
    echo "✅ Railway部署准备完成！"
    echo ""
    echo "📋 部署步骤："
    echo "1. 访问 https://railway.app"
    echo "2. 点击 'New Project' -> 'Deploy from GitHub repo'"
    echo "3. 选择此仓库"
    echo "4. 添加MySQL数据库服务"
    echo "5. 配置环境变量（Railway会自动识别）"
    echo "6. 部署完成！"
    ;;
    
  3)
    echo ""
    echo "📦 准备Render部署包..."
    
    echo ""
    echo "✅ Render部署准备完成！"
    echo ""
    echo "📋 部署步骤："
    echo "1. 访问 https://render.com"
    echo "2. 点击 'New' -> 'Blueprint'"
    echo "3. 连接此仓库"
    echo "4. Render会自动读取 render.yaml"
    echo "5. 点击 'Apply' 开始部署"
    echo "6. 部署完成！"
    ;;
    
  4)
    echo ""
    echo "🔨 打包Docker镜像..."
    
    IMAGE_NAME="tiku-system"
    IMAGE_TAG="latest"
    
    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
    
    echo ""
    echo "✅ 镜像打包完成！"
    echo ""
    echo "📦 镜像信息："
    docker images | grep ${IMAGE_NAME}
    echo ""
    echo "🚀 运行镜像："
    echo "   docker run -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}"
    ;;
    
  *)
    echo "❌ 无效选项"
    exit 1
    ;;
esac

echo ""
echo "=========================================="
echo "   部署流程完成！"
echo "=========================================="

