# ============================================
# 多数据源对接管理系统 - 多阶段构建 Dockerfile
# 技术栈: Spring Boot 3.2 + Vue 3 + Nginx
# ============================================

# ---- Stage 1: 前端构建 ----
FROM node:18-alpine AS frontend-build

WORKDIR /app

COPY frontend/package.json ./
RUN npm install --registry=https://registry.npmmirror.com

COPY frontend/ ./
RUN npm run build

# ---- Stage 2: 后端构建 ----
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-build

WORKDIR /app

# 使用阿里云 Maven 镜像加速依赖下载
COPY backend/settings.xml /root/.m2/settings.xml

COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn package -DskipTests -B

# ---- Stage 3: 最终运行镜像 ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装 Nginx 用于前端静态资源
RUN apk add --no-cache nginx

# 复制后端 JAR
COPY --from=backend-build /app/target/*.jar app.jar

# 复制前端构建产物到 Nginx 目录
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY frontend/nginx.conf /etc/nginx/http.d/default.conf

# 复制 MySQL 初始化脚本（可选，用于外部挂载）
COPY mysql/init.sh /docker-entrypoint-initdb.d/
COPY mysql/my.cnf /etc/mysql/conf.d/

# 创建启动脚本
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'nginx' >> /app/start.sh && \
    echo 'exec java -jar /app/app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

# 暴露端口：80(前端/Nginx), 8080(后端API)
EXPOSE 80 8080

# 环境变量（数据库连接配置）
ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=data_source_manager \
    DB_USER=root \
    DB_PASSWORD=root123 \
    JWT_SECRET=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwQUJDREVGR0g= \
    JAVA_OPTS="-Xms256m -Xmx512m"

CMD ["/app/start.sh"]
