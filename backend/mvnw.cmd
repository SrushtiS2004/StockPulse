@echo off
set "MAVEN_CMD=%USERPROFILE%\maven\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" %*
) else (
    call mvn %*
)
