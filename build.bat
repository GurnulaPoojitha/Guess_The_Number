@echo off
rem Compiles the game into the "out" folder. Needs Java 17 or newer (javac on the PATH).
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -encoding UTF-8 -d out -cp "lib\h2-2.2.224.jar" --source-path src src\com\guessword\Main.java
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)
echo Build OK. Start the game with run.bat
