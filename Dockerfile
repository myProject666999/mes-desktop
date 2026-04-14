# 使用 Ubuntu 作为基础镜像
FROM ubuntu:22.04

LABEL maintainer="MES System"

# 设置非交互式环境变量
ENV DEBIAN_FRONTEND=noninteractive

# 安装 Java 17 和必要的包
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libfontconfig1 \
    libfreetype6 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libasound2 \
    libpangocairo-1.0-0 \
    libpango-1.0-0 \
    libatk1.0-0 \
    libcairo2 \
    libgdk-pixbuf2.0-0 \
    libglib2.0-0 \
    libxss1 \
    libnss3 \
    libdrm2 \
    libgbm1 \
    x11vnc \
    xvfb \
    fluxbox \
    wmctrl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制应用程序 JAR
COPY target/javafx-mes-1.0.0.jar app.jar

# 创建启动脚本
RUN cat > /app/start.sh << 'EOF'
#!/bin/bash
export DISPLAY=:1
Xvfb :1 -screen 0 1280x800x24 &
sleep 2
fluxbox &
sleep 1
x11vnc -display :1 -nopw -forever -shared -rfbport 5900 &
sleep 2
java --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
     -jar app.jar
EOF

RUN chmod +x /app/start.sh

# 暴露 VNC 端口
EXPOSE 5900

ENTRYPOINT ["/app/start.sh"]
