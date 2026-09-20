#!/usr/bin/env bash
# Linux Startup Script for CTH Job Payment Platform
set -e

JAR_PATH="payment-api/target/payment-api-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Artifact not found. Building application first..."
    ./mvnw clean package
fi

echo "Starting CTH Job Payment Platform in background..."
nohup java -jar "$JAR_PATH" > application.log 2>&1 &
PID=$!
echo $PID > application.pid
echo "Application started with PID $PID. Logs redirected to application.log."
