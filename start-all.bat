@echo off
REM ============================================
REM Blog Platform - Start ALL (backend + frontend)
REM Opens two new windows.
REM ============================================
start "Blog-Backend" cmd /k "%~dp0start-backend.bat"
start "Blog-Frontend" cmd /k "%~dp0start-frontend.bat"
echo Both services are starting in separate windows...
echo Backend : http://localhost:8080
echo Frontend: http://localhost:5173
exit /b 0
