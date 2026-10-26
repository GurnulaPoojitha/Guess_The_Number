#!/bin/sh
# Compiles the game into the "out" folder (macOS / Linux). Needs Java 17 or newer.
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out -cp lib/h2-2.2.224.jar --source-path src src/com/guessword/Main.java
echo "Build OK. Start the game with ./run.sh"
