#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

mode="${1:-all}"
if [[ "$mode" != all && "$mode" != framework ]]; then
    echo "Usage : bash scripts/build.sh [all|framework]" >&2
    exit 1
fi

# Seuls les dossiers de compilation connus sont nettoyés.
rm -rf -- framework/build
mkdir -p framework/build/classes
mapfile -d '' framework_sources < <(find framework/src/main/java -name '*.java' -print0)
javac -parameters -encoding UTF-8 -cp 'framework/lib/*' \
    -d framework/build/classes "${framework_sources[@]}"
jar --create --file framework/build/framework.jar -C framework/build/classes .
echo "Framework construit : framework/build/framework.jar"

if [[ "$mode" == framework ]]; then
    exit 0
fi

rm -rf -- TestApplication/build
mkdir -p TestApplication/build/webapp/WEB-INF/classes TestApplication/build/webapp/WEB-INF/lib
cp -a TestApplication/src/main/webapp/. TestApplication/build/webapp/
web_lib=TestApplication/build/webapp/WEB-INF/lib
cp framework/build/framework.jar "$web_lib/"

# L'API Servlet sert à compiler ; Tomcat la fournit à l'exécution.
for dependency in framework/lib/*.jar TestApplication/lib/*.jar; do
    [[ -f "$dependency" ]] || continue
    name="${dependency##*/}"
    case "$name" in
        servlet-api.jar|jakarta.servlet-api*.jar) continue ;;
        framework.jar)
            echo "framework.jar doit être construit depuis framework/src, pas ajouté aux dépendances." >&2
            exit 1 ;;
    esac
    if [[ -f "$web_lib/$name" ]] && ! cmp -s "$dependency" "$web_lib/$name"; then
        echo "Deux versions différentes de la dépendance $name." >&2
        exit 1
    fi
    cp "$dependency" "$web_lib/"
done

mapfile -d '' app_sources < <(find TestApplication/src/main/java -name '*.java' -print0)
javac -parameters -encoding UTF-8 \
    -cp 'framework/lib/*:TestApplication/build/webapp/WEB-INF/lib/*' \
    -d TestApplication/build/webapp/WEB-INF/classes "${app_sources[@]}"
jar --create --file "TestApplication/build/$APP_NAME.war" -C TestApplication/build/webapp .
echo "Application construite : TestApplication/build/$APP_NAME.war"
