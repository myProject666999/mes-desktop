FROM dorowu/ubuntu-desktop-lxde-vnc:focal

LABEL maintainer="MES System"

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:1
ENV RESOLUTION=1920x1080

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libgtk2.0-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/javafx-mes-1.0.0.jar /app/app.jar

RUN mkdir -p /root/Desktop && \
    echo '[Desktop Entry]' > /root/Desktop/MES.desktop && \
    echo 'Type=Application' >> /root/Desktop/MES.desktop && \
    echo 'Name=MES System' >> /root/Desktop/MES.desktop && \
    echo 'Exec=java --add-modules javafx.controls,javafx.fxml -jar /app/app.jar' >> /root/Desktop/MES.desktop && \
    echo 'Icon=utilities-terminal' >> /root/Desktop/MES.desktop && \
    chmod +x /root/Desktop/MES.desktop

EXPOSE 80 5900

CMD ["/startup.sh"]
