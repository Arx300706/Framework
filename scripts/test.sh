#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

suite="${1:-all}"
case "$suite" in
    all|sprints|database|uploads) ;;
    *) echo "Usage : bash scripts/test.sh [all|sprints|database|uploads]" >&2; exit 1 ;;
esac

bash "$PROJECT_ROOT/scripts/build.sh"
TEST_BUILD_DIR="$(mktemp -d "${TMPDIR:-/tmp}/framework-tests.XXXXXX")"
trap 'rm -rf -- "$TEST_BUILD_DIR"' EXIT
CLASSPATH="$PROJECT_ROOT/TestApplication/build/webapp/WEB-INF/classes:$PROJECT_ROOT/TestApplication/build/webapp/WEB-INF/lib/*:$PROJECT_ROOT/framework/lib/servlet-api.jar"
mapfile -d '' test_sources < <(find TestApplication/src/test/java -name '*.java' -print0)
javac -parameters -encoding UTF-8 -cp "$CLASSPATH" \
    -d "$TEST_BUILD_DIR/classes" "${test_sources[@]}"

if [[ "$suite" == all || "$suite" == sprints ]]; then
    java '-Dapp.db.url=jdbc:h2:mem:sprint6;DB_CLOSE_DELAY=-1' -Dapp.db.user=sa -Dapp.db.password= \
        -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.Sprint6SmokeTest
fi
if [[ "$suite" == all || "$suite" == database ]]; then
    java '-Dapp.db.url=jdbc:h2:mem:repository;DB_CLOSE_DELAY=-1' -Dapp.db.user=sa -Dapp.db.password= \
        -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.PersonneRepositorySmokeTest
fi

if [[ "$suite" == all || "$suite" == sprints || "$suite" == uploads ]]; then
    java -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.Sprint8SmokeTest
    java -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.Sprint9SmokeTest
    java -cp "$TEST_BUILD_DIR/classes:$CLASSPATH" com.app.test.Sprint10SmokeTest
fi
