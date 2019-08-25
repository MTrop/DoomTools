@echo off
REM =========================================================================
SETLOCAL
SET JAVAEXE=
SET JAVAOPTS={{JAVA_OPTIONS}}
SET JAVACLASSPATH={{COMPILE_CLASSPATH}}
SET JAVAJAR={{JAR_NAME}}
SET MAINCLASS={{MAIN_CLASSNAME}}
REM =========================================================================

where java > nul
if ERRORLEVEL == 0 (SET JAVAEXE=java)
if not %JAVAEXE%=="" goto _calljava
if not %JAVA_HOME%=="" (SET JAVAEXE=%JAVA_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava
if not %JRE_HOME%=="" (SET JAVAEXE=%JRE_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava
if not %JDK_HOME%=="" (SET JAVAEXE=%JDK_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava
goto _end

:_calljava
"%JAVAEXE%" -cp "%~dp0\%JAVAJAR%" %JAVAOPTS% %MAINCLASS% %*

:_end
ENDLOCAL
