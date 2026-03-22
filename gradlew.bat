@echo off
where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle is not installed. Install Gradle 9+ or generate the Gradle wrapper with "gradle wrapper".
exit /b 1
