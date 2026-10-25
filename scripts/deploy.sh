#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

DEFAULT_TOMCAT_HOME="/home/armando/Documents/S3_S4/tomcat/apache-tomcat-10.0.16"
TOMCAT_PORT="${TOMCAT_PORT:-8081}"
if [[ -n "${TOMCAT_WEBAPPS:-}" ]]; then
    destination="$TOMCAT_WEBAPPS"
elif [[ -n "${CATALINA_BASE:-}" && -d "$CATALINA_BASE/webapps" ]]; then
    destination="$CATALINA_BASE/webapps"
elif [[ -d "$DEFAULT_TOMCAT_HOME/webapps" ]]; then
    destination="$DEFAULT_TOMCAT_HOME/webapps"
elif [[ -n "${CATALINA_HOME:-}" && -d "$CATALINA_HOME/webapps" ]]; then
    destination="$CATALINA_HOME/webapps"
elif [[ -d /var/lib/tomcat10/webapps ]]; then
    destination=/var/lib/tomcat10/webapps
else
    echo "Dossier Tomcat introuvable. Définis TOMCAT_WEBAPPS=/chemin/vers/tomcat/webapps." >&2
    exit 1
fi
if [[ ! -d "$destination" ]]; then
    echo "Dossier Tomcat introuvable : $destination" >&2
    exit 1
fi

bash "$PROJECT_ROOT/scripts/build.sh"
war="$PROJECT_ROOT/TestApplication/build/$APP_NAME.war"
target="$destination/$APP_NAME.war"
if ! cp -f -- "$war" "$target"; then
    echo "Copie refusée. Le WAR est prêt ; copie-le avec les droits nécessaires :" >&2
    printf 'sudo cp -- %q %q\n' "$war" "$target" >&2
    exit 1
fi
echo "Application déployée dans $target"
context="/$APP_NAME"
[[ "$APP_NAME" != ROOT ]] || context=""
echo "Formulaires : http://localhost:$TOMCAT_PORT$context/details"
echo "JSON : http://localhost:$TOMCAT_PORT$context/test1"
echo "Personnes : http://localhost:$TOMCAT_PORT$context/liste"
