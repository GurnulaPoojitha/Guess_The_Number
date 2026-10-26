#!/bin/sh
# Compiles the app and the tests, then runs every test (macOS / Linux).
set -e
cd "$(dirname "$0")"
rm -rf out-test
javac -encoding UTF-8 -d out-test -cp lib/h2-2.2.224.jar --source-path src:test test/com/guessword/TestRunner.java
java -Djava.awt.headless=true -cp out-test:lib/h2-2.2.224.jar com.guessword.TestRunner
