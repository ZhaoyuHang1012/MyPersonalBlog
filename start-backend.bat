@echo off
REM ============================================
REM Blog Platform - Backend Launcher
REM ============================================
cd /d "%~dp0backend"

set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JDK not found at %JAVA_HOME%
    echo Please edit this file and set JAVA_HOME to your JDK path.
    pause
    exit /b 1
)

if not exist "target\blog-backend-0.1.0.jar" (
    echo [INFO] JAR not found. Building backend first...
    call "..\tools\apache-maven-3.9.9\bin\mvn.cmd" -B -DskipTests package -s "..\tools\settings.xml" -gs "..\tools\settings.xml"
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
)

echo [INFO] Starting backend on port 8080 ...
"%JAVA_HOME%\bin\java.exe" -jar "target\blog-backend-0.1.0.jar"
pause
