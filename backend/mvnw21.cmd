@echo off
setlocal

REM Force Maven Wrapper to use Java 21 if installed in the default location.
if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
  set "JAVA_HOME=C:\Program Files\Java\jdk-21"
  set "PATH=%JAVA_HOME%\bin;%PATH%"
)

call "%~dp0mvnw.cmd" %*

