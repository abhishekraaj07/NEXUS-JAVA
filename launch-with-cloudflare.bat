@echo off
setlocal enabledelayedexpansion

echo ====================================================================
echo        NEXUS Private Chat -- Cloudflare Quick Tunnel Launcher
echo ====================================================================
echo.

set TOOLS_DIR=%~dp0tools
set CLOUDFLARED=%TOOLS_DIR%\cloudflared.exe

if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

if not exist "%CLOUDFLARED%" (
    echo [*] Downloading Cloudflare Tunnel binary (cloudflared.exe)...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe', '%CLOUDFLARED%')"
    if not exist "%CLOUDFLARED%" (
        echo [!] Failed to download cloudflared.exe. Please download manually from:
        echo     https://github.com/cloudflare/cloudflared/releases/latest
        pause
        exit /b 1
    )
    echo [*] cloudflared.exe downloaded successfully.
)

echo [*] Starting NEXUS Java 21 Backend on port 8080...
start "NEXUS-Backend" cmd /c "cd /d %~dp0nexus-backend && gradlew.bat bootRun"

echo [*] Waiting for local server to initialize on http://localhost:8080...
timeout /t 6 /nobreak >nul

echo [*] Initializing Cloudflare Zero-Knowledge Quick Tunnel...
echo [*] Your public HTTPS & WSS URL will appear below:
echo.
"%CLOUDFLARED%" tunnel --url http://localhost:8080

pause
