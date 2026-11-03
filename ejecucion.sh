#!/bin/bash

# ==========================================
# KOTLONIES - INICIAR FRONTEND Y BACKEND
# ==========================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

BACKEND_DIR="$PROJECT_DIR/backend/kotlonies-api"
FRONTEND_DIR="$PROJECT_DIR/frontend"

echo "=========================================="
echo "       INICIANDO KOTLONIES"
echo "=========================================="

# Comprobar directorios
if [ ! -d "$BACKEND_DIR" ] || [ ! -d "$FRONTEND_DIR" ]; then
    echo "ERROR: No se encuentran las carpetas del proyecto."
    exit 1
fi

# Iniciar backend
echo "[1/2] Iniciando backend Spring Boot..."

gnome-terminal -- bash -c "
    cd '$BACKEND_DIR'
    ./mvnw spring-boot:run
    exec bash
" 2>/dev/null || x-terminal-emulator -e bash -c "
    cd '$BACKEND_DIR'
    ./mvnw spring-boot:run
    exec bash
" &

# Iniciar frontend
echo "[2/2] Iniciando frontend Angular..."

gnome-terminal -- bash -c "
    cd '$FRONTEND_DIR'
    npm start
    exec bash
" 2>/dev/null || x-terminal-emulator -e bash -c "
    cd '$FRONTEND_DIR'
    npm start
    exec bash
" &

echo "=========================================="
echo "Kotlonies se está iniciando."
echo "Frontend: http://localhost:4200"
echo "Backend:  http://localhost:8080"
echo "=========================================="
