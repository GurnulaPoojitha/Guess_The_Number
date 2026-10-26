@echo off
rem Compiles the app and the tests, then runs every test. Uses an in-memory database,
rem so your saved games in the "data" folder are not touched.
cd /d "%~dp0"
if exist out-test rmdir /s /q out-test
javac -encoding UTF-8 -d out-test -cp "lib\h2-2.2.224.jar" --source-path "src;test" test\com\guessword\TestRunner.java
if errorlevel 1 (
    echo Test build failed.
    exit /b 1
)
java -Djava.awt.headless=true -cp "out-test;lib\h2-2.2.224.jar" com.guessword.TestRunner
