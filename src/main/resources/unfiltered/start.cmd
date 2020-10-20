@echo off

if "%1"=="-help" goto help
goto run

:help
echo.
echo Unity Intelligence Engine Performance Tests. Copyright (c) 2005-2019 Intellective Inc.
echo.
echo.
echo.
goto :EOF

:run

if not exist logs md logs

:ID
"%JAVA_HOME%\bin\java" -Xmx3072m -XX:MaxGCPauseMillis=200 -XX:+UseG1GC -jar uie-performance-test.jar
goto EOF


:EOF