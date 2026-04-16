#!/bin/bash

Xvfb :0 -screen 0 1280x800x24 &
sleep 2

fluxbox -display :0 &
sleep 1

x11vnc -display :0 -forever -shared -rfbauth /root/.vnc/passwd &
sleep 1

cd /usr/share/novnc
websockify --web=/usr/share/novnc 6080 localhost:5900 &
sleep 1

cd /app
java --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
     -jar app.jar
