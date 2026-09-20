@echo off
REM Windows Startup Script for CTH Job Payment Platform
echo Starting CTH Job Payment Platform on Windows...

if not exist "payment-api\target\payment-api-1.0.0-SNAPSHOT.jar" (
    echo Artifact not found. Building application first...
    call build.bat
)

start "CTH Job Payment Platform" java -jar payment-api\target\payment-api-1.0.0-SNAPSHOT.jar
echo Application startup initiated.
