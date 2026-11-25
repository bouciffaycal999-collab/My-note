@echo off
setlocal
cd /d "%~dp0"
title CERAMICA POS - Serveur PC (Port 8173)

echo ==================================================
echo   CERAMICA POS - Version PC (Navigateur)
echo   Demarrage du serveur local...
echo ==================================================
echo.

if not exist "index.html" (
  echo [ATTENTION] Les fichiers ne sont pas encore extraits du ZIP !
  echo.
  echo Faites clic droit sur le fichier .zip telecharge -^> "Extraire tout..."
  echo puis ouvrez le dossier extrait "pc-web" et relancez Start_CERAMICA_PC.bat.
  echo.
  pause
  exit /b 1
)

where powershell >nul 2>nul
if %errorlevel%==0 (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0server.ps1"
  if %errorlevel%==0 goto :end
)

py -3 -c "import http.server" >nul 2>nul
if %errorlevel%==0 (
  start "" "http://localhost:8173"
  py -3 -m http.server 8173
  goto :end
)

python -c "import http.server" >nul 2>nul
if %errorlevel%==0 (
  start "" "http://localhost:8173"
  python -m http.server 8173
  goto :end
)

where npx >nul 2>nul
if %errorlevel%==0 (
  start "" "http://localhost:8173"
  npx --yes serve -l 8173 .
  goto :end
)

echo [ERREUR] Impossible de demarrer le serveur local sur ce PC.
echo Essayez d'installer Python (python.org) ou Node.js (nodejs.org) puis relancez.
pause

:end
endlocal
