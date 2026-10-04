#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TEST_BUILD_DIR="$(mktemp -d "${TMPDIR:-/tmp}/sprint6-tests.XXXXXX")"
trap 'rm -rf "$TEST_BUILD_DIR"' EXIT
cd "$PROJECT_ROOT"

CLASSPATH="$PROJECT_ROOT/framework/lib/*:$SCRIPT_DIR/src/main/webapp/WEB-INF/lib/*"
find framework/src/main/java TestApplication/src/main/java TestApplication/src/test/java -name '*.java' > "$TEST_BUILD_DIR/sources.txt"
javac -encoding UTF-8 -cp "$CLASSPATH" -d "$TEST_BUILD_DIR/classes" @"$TEST_BUILD_DIR/sources.txt"

java '-Dapp.db.url=jdbc:h2:mem:sprint6;DB_CLOSE_DELAY=-1' -Dapp.db.user=sa -Dapp.db.password= \
    -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.Sprint6SmokeTest
