@echo off
REM Windows Build Script for CTH Job Payment Platform
echo ===================================================
echo Building CTH Job International Payment Platform...
echo ===================================================

call mvnw.cmd clean package

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed! Check Maven output for details.
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Build completed successfully. Application packaged in payment-api/target/payment-api-1.0.0-SNAPSHOT.jar
