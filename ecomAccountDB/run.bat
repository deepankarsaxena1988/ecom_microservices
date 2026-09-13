@echo off
cd /d "%~dp0"

docker compose down -v
docker compose up -d --build --force-recreate

if %ERRORLEVEL% NEQ 0 (
    echo Failed to start ecomAccounts DB container.
    exit /b %ERRORLEVEL%
)

echo ecomAccounts DB started successfully.
