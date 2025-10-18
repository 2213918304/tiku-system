# 多阶段构建 - 前端
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY tiku-frontend/package*.json ./
RUN npm install
COPY tiku-frontend/ ./
# 创建生产环境配置
RUN echo "VITE_API_BASE_URL=/api" > .env.production && \
    echo "VITE_APP_TITLE=辽师大题库系统" >> .env.production
RUN npm run build

# 多阶段构建 - 后端（包含前端）
FROM maven:3.8.6-openjdk-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 复制前端构建文件到Spring Boot的static目录
COPY --from=frontend-build /app/dist ./src/main/resources/static/
# 构建jar包（前端文件会被打包进去）
RUN mvn clean package -DskipTests -Pprod

# 最终镜像
FROM openjdk:17-jre-slim
WORKDIR /app

# 安装curl（用于健康检查）、中文字体和时区数据
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    fonts-wqy-zenhei \
    tzdata && \
    rm -rf /var/lib/apt/lists/*

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制后端jar包（已包含前端文件）
COPY --from=backend-build /app/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/logs /app/uploads /app/temp

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dspring.profiles.active=prod -Dfile.encoding=UTF-8"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

