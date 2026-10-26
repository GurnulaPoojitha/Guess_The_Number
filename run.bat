@echo off
rem Builds the game if needed, then starts it. Data is saved in the "data" folder next to this file.
cd /d "%~dp0"
if not exist out\com\guessword\Main.class (
    call "%~dp0build.bat"
    if errorlevel 1 exit /b 1
)
java -cp "out;lib\h2-2.2.224.jar" com.guessword.Main
