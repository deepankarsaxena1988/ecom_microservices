@echo off
REM ============================================================
REM run.bat - Starts / restarts / builds ecom microservices.
REM
REM Usage:
REM   run.bat                          -> starts ecomEureka, ecomConfigServer,
REM                                       ecomAPIGateWay (in order, with health checks)
REM   run.bat <serviceName>            -> starts only the given service
REM   run.bat restart <serviceName>    -> stops (if running) and restarts the given service
REM   run.bat stop <serviceName>       -> stops the given service (if currently running)
REM   run.bat build <serviceName>      -> runs "mvn clean install" for the given project
REM   run.bat build <serviceName> <goal(s)> -> runs the given maven goal(s) instead of "clean install"
REM
REM Supported service names: ecomEureka, ecomConfigServer, ecomAPIGateWay
REM (any other folder name directly under this directory containing a
REM  pom.xml is also supported, e.g. run.bat ecomProductList)
REM ============================================================

SETLOCAL EnableDelayedExpansion
SET BASE_DIR=%~dp0

IF /I "%~1"=="build" (
    IF "%~2"=="" (
        echo Usage: run.bat build ^<serviceName^> [maven-goal^(s^), default: clean install]
        GOTO :EOF
    )
    CALL :BUILD_SERVICE "%~2" "%~3" "%~4" "%~5"
    GOTO :EOF
)

IF /I "%~1"=="stop" (
    IF "%~2"=="" (
        echo Usage: run.bat stop ^<serviceName^>
        GOTO :EOF
    )
    CALL :STOP_SERVICE "%~2"
    GOTO :EOF
)

IF /I "%~1"=="restart" (
    IF "%~2"=="" (
        echo Usage: run.bat restart ^<serviceName^>
        GOTO :EOF
    )
    CALL :STOP_SERVICE "%~2"
    CALL :START_SERVICE "%~2"
    GOTO :EOF
)

IF NOT "%~1"=="" (
    CALL :START_SERVICE "%~1"
    GOTO :EOF
)

echo [1/3] Starting ecomEureka (port 5000)...
CALL :START_SERVICE ecomEureka

echo Waiting for ecomEureka to become available...
:WAIT_EUREKA
timeout /t 5 /nobreak >nul
powershell -NoProfile -Command "try { (Invoke-WebRequest -Uri http://localhost:5000/dashboard -UseBasicParsing -TimeoutSec 2) | Out-Null; exit 0 } catch { exit 1 }"
if errorlevel 1 goto WAIT_EUREKA
echo ecomEureka is up.

echo [2/3] Starting ecomConfigServer (port 8888)...
CALL :START_SERVICE ecomConfigServer

echo Waiting for ecomConfigServer to become available...
:WAIT_CONFIG
timeout /t 5 /nobreak >nul
powershell -NoProfile -Command "try { (Invoke-WebRequest -Uri http://localhost:8888/actuator/health -UseBasicParsing -TimeoutSec 2) | Out-Null; exit 0 } catch { exit 1 }"
if errorlevel 1 goto WAIT_CONFIG
echo ecomConfigServer is up.

echo [3/3] Starting ecomAPIGateWay (port 4000)...
CALL :START_SERVICE ecomAPIGateWay

echo All services have been launched:
echo   - ecomEureka:       http://localhost:5000/dashboard
echo   - ecomConfigServer: http://localhost:8888
echo   - ecomAPIGateWay:   http://localhost:4000

ENDLOCAL
GOTO :EOF

REM ------------------------------------------------------------
REM :START_SERVICE <serviceName>
REM Resolves the project folder for a known service name and
REM launches it in its own console window via mvnw spring-boot:run.
REM ------------------------------------------------------------
:START_SERVICE
SET SERVICE_NAME=%~1
CALL :RESOLVE_SERVICE_DIR "%SERVICE_NAME%"
IF ERRORLEVEL 1 EXIT /B 1

echo Starting %SERVICE_NAME% from %SERVICE_DIR% ...
IF EXIST "%SERVICE_DIR%\mvnw.cmd" (
    SET RUN_CMD=.\mvnw.cmd spring-boot:run
) ELSE (
    SET RUN_CMD=mvn spring-boot:run
)

IF NOT EXIST "%BASE_DIR%.run" MKDIR "%BASE_DIR%.run"

REM Generate a small launcher script per service to avoid fragile
REM nested quoting/escaping when starting it through PowerShell.
SET LAUNCHER=%BASE_DIR%.run\%SERVICE_NAME%_launch.cmd
(
    echo @echo off
    echo cd /d "%SERVICE_DIR%"
    echo %RUN_CMD%
) > "%LAUNCHER%"

FOR /F "usebackq delims=" %%P IN (`powershell -NoProfile -Command "(Start-Process -FilePath '%ComSpec%' -ArgumentList '/k','\"%LAUNCHER%\"' -WindowStyle Normal -PassThru).Id"`) DO SET NEW_PID=%%P

echo %NEW_PID%> "%BASE_DIR%.run\%SERVICE_NAME%.pid"
echo %SERVICE_NAME% started with PID %NEW_PID%.
EXIT /B 0

REM ------------------------------------------------------------
REM :BUILD_SERVICE <serviceName> [goal1] [goal2] [goal3]
REM Resolves the project folder for the given service name and runs
REM a Maven build (default goals: clean install) synchronously in
REM the current console, showing full build output.
REM ------------------------------------------------------------
:BUILD_SERVICE
SET SERVICE_NAME=%~1
CALL :RESOLVE_SERVICE_DIR "%SERVICE_NAME%"
IF ERRORLEVEL 1 EXIT /B 1

SET BUILD_GOALS=%~2 %~3 %~4
SET BUILD_GOALS=%BUILD_GOALS: =%
IF "%BUILD_GOALS%"=="" (
    SET BUILD_GOALS=clean install
) ELSE (
    SET BUILD_GOALS=%~2 %~3 %~4
)

echo Building %SERVICE_NAME% in %SERVICE_DIR% with goal(s): %BUILD_GOALS% ...
PUSHD "%SERVICE_DIR%"
IF EXIST "%SERVICE_DIR%\mvnw.cmd" (
    CALL .\mvnw.cmd %BUILD_GOALS%
) ELSE (
    CALL mvn %BUILD_GOALS%
)
SET BUILD_RESULT=%ERRORLEVEL%
POPD

IF %BUILD_RESULT% EQU 0 (
    echo Build succeeded for %SERVICE_NAME%.
) ELSE (
    echo Build FAILED for %SERVICE_NAME% ^(exit code %BUILD_RESULT%^).
)
EXIT /B %BUILD_RESULT%

REM ------------------------------------------------------------
REM :RESOLVE_SERVICE_DIR <serviceName>
REM Sets SERVICE_DIR for a known service name, or any other folder
REM directly under BASE_DIR containing a pom.xml. Sets errorlevel 1
REM and prints an error if the service cannot be resolved.
REM ------------------------------------------------------------
:RESOLVE_SERVICE_DIR
SET RESOLVE_NAME=%~1
SET SERVICE_DIR=

IF /I "%RESOLVE_NAME%"=="ecomEureka" SET SERVICE_DIR=%BASE_DIR%ecomEureka\eureka
IF /I "%RESOLVE_NAME%"=="ecomConfigServer" SET SERVICE_DIR=%BASE_DIR%ecomConfigServer
IF /I "%RESOLVE_NAME%"=="ecomAPIGateWay" SET SERVICE_DIR=%BASE_DIR%ecomAPIGateWay

IF "%SERVICE_DIR%"=="" (
    IF EXIST "%BASE_DIR%%RESOLVE_NAME%\pom.xml" (
        SET SERVICE_DIR=%BASE_DIR%%RESOLVE_NAME%
    ) ELSE (
        echo Unknown service "%RESOLVE_NAME%". Supported: ecomEureka, ecomConfigServer, ecomAPIGateWay
        echo ^(or any other folder directly under %BASE_DIR% containing a pom.xml^)
        EXIT /B 1
    )
)
EXIT /B 0

REM ------------------------------------------------------------
REM :STOP_SERVICE <serviceName>
REM Kills the previously launched console (and its child mvn/java
REM process tree) for the given service, using the PID recorded by
REM START_SERVICE in .run\<serviceName>.pid. Falls back to matching
REM the console window title if no PID file is found.
REM ------------------------------------------------------------
:STOP_SERVICE
SET STOP_NAME=%~1
SET STOP_PID_FILE=%BASE_DIR%.run\%STOP_NAME%.pid

IF EXIST "%STOP_PID_FILE%" (
    SET /P STOP_PID=<"%STOP_PID_FILE%"
    echo Stopping %STOP_NAME% ^(PID !STOP_PID!^)...
    taskkill /PID !STOP_PID! /T /F >nul 2>&1
    DEL /F /Q "%STOP_PID_FILE%" >nul 2>&1
) ELSE (
    echo Stopping %STOP_NAME% ^(if currently running^)...
    taskkill /FI "WINDOWTITLE eq %STOP_NAME%*" /T /F >nul 2>&1
)
EXIT /B 0
