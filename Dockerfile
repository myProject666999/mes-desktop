FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="MES System"

RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download and install JavaFX SDK
RUN wget -O javafx-sdk.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip && \
    unzip javafx-sdk.zip -d /opt && \
    rm javafx-sdk.zip

ENV PATH_TO_FX=/opt/javafx-sdk-21.0.2/lib

WORKDIR /app

COPY target/javafx-mes-1.0.0.jar app.jar

EXPOSE 8080

ENV DISPLAY=:0

ENTRYPOINT ["java", "--module-path", "/opt/javafx-sdk-21.0.2/lib", "--add-modules", "javafx.controls,javafx.fxml", "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED", "-jar", "app.jar"]
