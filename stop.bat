@echo off
REM Windows Shutdown Script for CTH Job Payment Platform
echo Stopping CTH Job Payment Platform...

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Terminating PID %%a listening on port 8080...
    taskkill /F /PID %%a
)

echo Application stopped.
