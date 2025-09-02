#!/bin/bash

# GlobeMed Healthcare System Launcher Script
# This script helps you run the application with custom database configuration

echo "🏥 GlobeMed Healthcare System Launcher"
echo "======================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install Java 17+ and try again"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Error: Java 17+ is required (found Java $JAVA_VERSION)"
    echo "Please upgrade Java and try again"
    exit 1
fi

# Check if JAR file exists
JAR_FILE="target/healthcare-system-1.0.0-executable.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first: mvn clean package"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"
echo "✅ JAR file found"
echo ""

# Prompt for database configuration if not set
if [ -z "$DB_URL" ]; then
    echo "📋 Database Configuration"
    echo "========================="
    read -p "Database URL (default: jdbc:mysql://localhost:3306/globemed_db): " input_url
    export DB_URL="${input_url:-jdbc:mysql://localhost:3306/globemed_db}"
fi

if [ -z "$DB_USERNAME" ]; then
    read -p "Database Username (default: root): " input_username
    export DB_USERNAME="${input_username:-root}"
fi

if [ -z "$DB_PASSWORD" ]; then
    read -s -p "Database Password: " input_password
    echo ""
    if [ -n "$input_password" ]; then
        export DB_PASSWORD="$input_password"
    fi
fi

echo ""
echo "🚀 Starting GlobeMed Healthcare System..."
echo "Database: $DB_URL"
echo "Username: $DB_USERNAME"
echo ""

# Run the application
java -jar "$JAR_FILE" "$@"