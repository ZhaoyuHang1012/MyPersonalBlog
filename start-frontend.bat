@echo off
REM ============================================
REM Blog Platform - Frontend Launcher
REM ============================================
cd /d "%~dp0frontend"

where node >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Node.js not found in PATH.
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo [INFO] node_modules not found. Installing dependencies first...
    call npm install --no-audit --no-fund
    if errorlevel 1 (
        echo [ERROR] npm install failed.
        pause
        exit /b 1
    )
)

echo [INFO] Starting frontend on port 5173 ...
call npm run dev
pause
