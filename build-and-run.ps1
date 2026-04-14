# MES Desktop Application Build and Run Script
# This script builds the Java application and runs it in Docker with VNC access

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "MES Desktop - Build and Run Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build the application with Maven
Write-Host "Step 1: Building application with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    exit 1
}

Write-Host "Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Build and run Docker container
Write-Host "Step 2: Building and starting Docker container..." -ForegroundColor Yellow
docker-compose down
docker-compose up --build -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker operation failed! Please check Docker is running." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "MES Desktop is running!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access the application desktop via VNC:" -ForegroundColor Cyan
Write-Host "  VNC Address: localhost:5900" -ForegroundColor White
Write-Host "  (No password required)" -ForegroundColor White
Write-Host ""
Write-Host "To connect using a VNC client:" -ForegroundColor Cyan
Write-Host "  1. Download and install a VNC client (e.g., RealVNC, TightVNC, or TigerVNC)" -ForegroundColor White
Write-Host "  2. Connect to: localhost:5900" -ForegroundColor White
Write-Host ""
Write-Host "To stop the container:" -ForegroundColor Cyan
Write-Host "  docker-compose down" -ForegroundColor White
Write-Host ""
Write-Host "To view container logs:" -ForegroundColor Cyan
Write-Host "  docker-compose logs -f" -ForegroundColor White
Write-Host ""
