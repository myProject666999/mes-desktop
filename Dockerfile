FROM bellsoft/liberica-openjdk-debian:17

LABEL maintainer="MES System"

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:1

RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    openbox \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/javafx-mes-1.0.0.jar app.jar

RUN mkdir -p /root/.vnc \
    && x11vnc -storepasswd mes123 /root/.vnc/passwd \
    && ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html

EXPOSE 5901 8080

RUN echo '#!/bin/bash\n\
echo "========================================"\n\
echo "MES Desktop - Docker"\n\
echo "========================================"\n\
echo "Starting services..."\n\
Xvfb :1 -screen 0 1280x800x16 -ac &\n\
sleep 2\n\
openbox --replace &\n\
sleep 1\n\
x11vnc -display :1 -rfbauth /root/.vnc/passwd -forever -shared -rfbport 5901 -q &\n\
sleep 1\n\
websockify -D --web=/usr/share/novnc/ 8080 localhost:5901 > /dev/null 2>&1\n\
sleep 2\n\
echo ""\n\
echo "✅  Services started!"\n\
echo "🌐  Browser: http://localhost:8080"\n\
echo "🔐  VNC Password: mes123"\n\
echo ""\n\
echo "Starting MES application..."\n\
echo "========================================"\n\
java --add-modules javafx.controls,javafx.fxml,javafx.graphics --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED -Dprism.order=sw -Djava.awt.headless=false -jar app.jar\n'\
> /app/start.sh && chmod +x /app/start.sh

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD timeout 5 bash -c 'cat < /dev/null > /dev/tcp/localhost/8080' || exit 1

ENTRYPOINT ["/bin/bash", "/app/start.sh"]
