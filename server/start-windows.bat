@echo off
copy /Y .env.example .env >nul 2>&1
where docker >nul 2>&1
if errorlevel 1 (
  echo Docker Desktop belum terpasang/aktif.
  pause
  exit /b 1
)
docker compose up -d --build
if errorlevel 1 pause
