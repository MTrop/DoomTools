@echo off

REM ============================[ VARIABLES ]================================
SETLOCAL
SET JAVAEXE=
SET JAVAOPTS={{JAVA_OPTIONS}}
SET JAVAJAR={{JAR_NAME}}
SET MAINCLASS={{MAIN_CLASSNAME}}
REM =========================================================================

REM ======= Java Scan =======

if exist "%~dp0\jre\bin\java.exe" SET JAVAEXE=%~dp0\jre\bin\java.exe
if not "%JAVAEXE%"=="" goto _calljava

where java > nul
if ERRORLEVEL == 0 SET JAVAEXE=java
if not "%JAVAEXE%"=="" goto _calljava

if not %JAVA_HOME%=="" SET JAVAEXE=%JAVA_HOME%\bin
if not "%JAVAEXE%"=="" goto _calljava

if not %JRE_HOME%=="" SET JAVAEXE=%JRE_HOME%\bin
if not "%JAVAEXE%"=="" goto _calljava

if not %JDK_HOME%=="" SET JAVAEXE=%JDK_HOME%\bin
if not "%JAVAEXE%"=="" goto _calljava

goto _end

REM =========================

:_calljava
"%JAVAEXE%" -cp "%~dp0\%JAVAJAR%" %JAVAOPTS% %MAINCLASS% %*

:_end
ENDLOCAL
