FROM openjdk:17-jdk-slim

LABEL maintainer="MES System"

RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/javafx-mes-1.0.0.jar app.jar

EXPOSE 8080

ENV DISPLAY=:0

ENTRYPOINT ["java", "--add-modules", "javafx.controls,javafx.fxml", "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED", "-jar", "app.jar"]
