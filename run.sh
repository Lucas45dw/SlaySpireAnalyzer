#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Slay the Spire 2 Strategy Analyzer ==="

mkdir -p "$DIR/out"

javac -cp "$DIR/libs/sqlite-jdbc-3.51.1.0.jar" -d "$DIR/out" \
    "$DIR/src/main/java/com/slayspire/analyzer/models/"*.java \
    "$DIR/src/main/java/com/slayspire/analyzer/database/"*.java \
    "$DIR/src/main/java/com/slayspire/analyzer/ui/"*.java \
    "$DIR/src/main/java/com/slayspire/analyzer/Main.java"

echo "Launching..."
java -cp "$DIR/out:$DIR/libs/sqlite-jdbc-3.51.1.0.jar" \
     -Ddb.path="$DIR/data.db" \
     com.slayspire.analyzer.Main
