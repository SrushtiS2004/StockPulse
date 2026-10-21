@echo off
title StockPulse AI Commerce Engine - Launcher
echo ======================================================================
echo                STOCKPULSE COMMERCE ^& INVENTORY ENGINE
echo ======================================================================
echo.
echo [1/2] Starting Spring Boot Backend on port 8081...
start "StockPulse-Backend" cmd /k "cd backend && mvnw.cmd spring-boot:run"
timeout /t 5 >nul
echo.
echo [2/2] Starting Vite Frontend on port 5173...
start "StockPulse-Frontend" cmd /k "cd frontend && npm run dev"
echo.
echo ======================================================================
echo  All systems booting!
echo  - Storefront Dashboard:  http://localhost:5173
echo  - REST API Engine:       http://localhost:8081/api/products
echo  - H2 Database Console:   http://localhost:8081/h2-console
echo ======================================================================
