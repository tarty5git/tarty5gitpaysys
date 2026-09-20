#!/usr/bin/env bash
# Linux Shutdown Script for CTH Job Payment Platform
set -e

if [ -f "application.pid" ]; then
    PID=$(cat application.pid)
    echo "Stopping CTH Job Payment Platform process with PID $PID..."
    kill "$PID" 2>/dev/null || true
    rm application.pid
    echo "Application stopped."
else
    echo "No application.pid file found. Searching for running process on port 8080..."
    PID=$(lsof -t -i:8080 2>/dev/null || true)
    if [ -n "$PID" ]; then
        kill -9 "$PID"
        echo "Terminated process $PID running on port 8080."
    else
        echo "No running application found."
    fi
fi
