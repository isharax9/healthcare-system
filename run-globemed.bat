@echo off
:: GlobeMed Healthcare System Launcher Script for Windows
:: This script helps you run the application with custom database configuration

echo 🏥 GlobeMed Healthcare System Launcher
echo =======================================

:: Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Java is not installed or not in PATH
    echo Please install Java 17+ and try again
    pause
    exit /b 1
)

:: Check if JAR file exists
set JAR_FILE=target\healthcare-system-1.0.0-executable.jar
if not exist "%JAR_FILE%" (
    echo ❌ Error: JAR file not found at %JAR_FILE%
    echo Please build the project first: mvn clean package
    pause
    exit /b 1
)

echo ✅ Java detected
echo ✅ JAR file found
echo.

:: Prompt for database configuration if not set
if "%DB_URL%"=="" (
    echo 📋 Database Configuration
    echo =========================
    set /p "input_url=Database URL (default: jdbc:mysql://localhost:3306/globemed_db): "
    if "%input_url%"=="" (
        set "DB_URL=jdbc:mysql://localhost:3306/globemed_db"
    ) else (
        set "DB_URL=%input_url%"
    )
)

if "%DB_USERNAME%"=="" (
    set /p "input_username=Database Username (default: root): "
    if "%input_username%"=="" (
        set "DB_USERNAME=root"
    ) else (
        set "DB_USERNAME=%input_username%"
    )
)

if "%DB_PASSWORD%"=="" (
    set /p "DB_PASSWORD=Database Password: "
)

echo.
echo 🚀 Starting GlobeMed Healthcare System...
echo Database: %DB_URL%
echo Username: %DB_USERNAME%
echo.

:: Run the application
java -jar "%JAR_FILE%" %*