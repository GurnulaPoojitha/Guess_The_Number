#!/bin/sh
# Builds the game if needed, then starts it (macOS / Linux).
set -e
cd "$(dirname "$0")"
[ -f out/com/guessword/Main.class ] || ./build.sh
java -cp out:lib/h2-2.2.224.jar com.guessword.Main
