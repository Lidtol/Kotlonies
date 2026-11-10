
@echo off
setlocal

title Kotlonies - Iniciador

echo ========================================
echo          INICIANDO KOTLONIES
echo ========================================
echo.

REM Obtener la carpeta donde esta este script
set "ROOT=%~dp0"

REM Rutas del proyecto
set "BACKEND=%ROOT%backend\kotlonies-api"
set "FRONTEND=%ROOT%frontend"

REM Comprobar backend
if not exist "%BACKEND%\mvnw.cmd" (
    echo ERROR: No se encuentra mvnw.cmd
    echo Ruta esperada: %BACKEND%
    pause
    exit /b 1
)

REM Comprobar frontend
if not exist "%FRONTEND%\package.json" (
    echo ERROR: No se encuentra package.json
    echo Ruta esperada: %FRONTEND%
    pause
    exit /b 1
)

REM Comprobar dependencias de Angular
if not exist "%FRONTEND%\node_modules" (
    echo No se encuentran las dependencias de Angular.
    echo Ejecutando npm install...
    cd /d "%FRONTEND%"
    call npm install
    if errorlevel 1 (
        echo ERROR: No se pudieron instalar las dependencias.
        pause
        exit /b 1
    )
)

echo Iniciando backend...
start "Kotlonies - Backend" cmd /k "cd /d ""%BACKEND%"" && call mvnw.cmd spring-boot:run"

timeout /t 3 /nobreak >nul

echo Iniciando frontend...
start "Kotlonies - Frontend" cmd /k "cd /d ""%FRONTEND%"" && call npm start"

echo.
echo ========================================
echo       KOTLONIES SE ESTA INICIANDO
echo ========================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:4200
echo.
echo Puedes cerrar esta ventana.
echo Las otras dos ventanas mantienen los servidores activos.
echo.

pause
