@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM  CSV Data Analyzer - Launcher
REM  Author: Sboniso Mathebula
REM ============================================================

title CSV Data Analyzer - Sboniso Mathebula
color 0A

REM ---------- Move to project root ----------
cd /d "%~dp0.."

REM ---------- Check data folder ----------
if not exist "data" (
    echo [ERROR] The "data" folder does not exist.
    pause
    exit /b 1
)

REM ---------- Compile ----------
echo.
echo [1/3] Compiling source files...
if exist "out" rmdir /s /q "out"
mkdir "out"

javac -d out src\main\java\*.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
echo       Compilation successful.

REM ---------- List CSV files ----------
echo.
echo [2/3] CSV files available in "data":
set "count=0"
for %%F in (data\*.csv) do (
    set /a count+=1
    echo       !count!. %%~nxF
)

if !count!==0 (
    echo [ERROR] No CSV files found in the "data" folder.
    pause
    exit /b 1
)

REM ---------- Prompt ----------
echo.
echo [3/3] Enter the CSV filename to analyze.
set /p "file=      Filename (e.g. test2.csv): "

if "!file!"=="" (
    echo [ERROR] No filename entered.
    pause
    exit /b 1
)

set "fullpath=data\!file!"

if not exist "!fullpath!" (
    echo [ERROR] File not found: !fullpath!
    pause
    exit /b 1
)

REM ---------- Run (pass file as argument, no piping) ----------
echo.
echo Running analysis on: !fullpath!
echo ------------------------------------------------------------
echo.

java -cp out Main "!fullpath!"

echo.
echo ------------------------------------------------------------
echo Done.
pause
endlocal