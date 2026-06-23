#!/bin/bash
set -e

# Définition des variables
APP_NAME="${APP_NAME:-testApplication}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit

SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp/WEB-INF"
# placer les jars applicatives dans WEB-INF/lib
LIB_DIR="src/main/webapp/WEB-INF/lib"
BUILD_DIR="build"
DEFAULT_TOMCAT_HOME="/home/armando/Documents/S3_S4/tomcat/apache-tomcat-10.0.16"
SYSTEM_TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
TOMCAT_PORT="${TOMCAT_PORT:-8081}"
if [ -n "$TOMCAT_WEBAPPS" ]; then
  TOMCAT_WEBAPPS="$TOMCAT_WEBAPPS"
elif [ -n "$CATALINA_BASE" ] && [ -d "$CATALINA_BASE/webapps" ]; then
  TOMCAT_WEBAPPS="$CATALINA_BASE/webapps"
elif [ -d "$DEFAULT_TOMCAT_HOME/webapps" ]; then
  TOMCAT_WEBAPPS="$DEFAULT_TOMCAT_HOME/webapps"
elif [ -n "$CATALINA_HOME" ] && [ -d "$CATALINA_HOME/webapps" ]; then
  TOMCAT_WEBAPPS="$CATALINA_HOME/webapps"
elif [ -d "$SYSTEM_TOMCAT_WEBAPPS" ]; then
  TOMCAT_WEBAPPS="$SYSTEM_TOMCAT_WEBAPPS"
else
  TOMCAT_WEBAPPS="$DEFAULT_TOMCAT_HOME/webapps"
fi
SERVLET_API_JAR="$LIB_DIR/*"

# Nettoyage et création du répertoire temporaire
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# Compilation des fichiers Java en utilisant les jars dans WEB-INF/lib (framework.jar + éventuels ...)
find "$SRC_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"
javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR/WEB-INF/classes" @"$BUILD_DIR/sources.txt"
rm "$BUILD_DIR/sources.txt"

# Copier les fichiers web (web.xml, JSP, etc.) et les libs
cp -r "$WEB_DIR/../"* "$BUILD_DIR/"   # copie des resources web (views, WEB-INF, ...)
cp -r "$LIB_DIR"/* "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null || true

# Générer le fichier .war dans le dossier build
cd "$BUILD_DIR" || exit
jar -cvf "$APP_NAME.war" *
cd ..

# Déploiement dans Tomcat
if [ ! -d "$TOMCAT_WEBAPPS" ]; then
  echo "Erreur: dossier Tomcat introuvable: $TOMCAT_WEBAPPS"
  echo "Corrige TOMCAT_WEBAPPS ou lance par exemple:"
  echo "TOMCAT_WEBAPPS=/chemin/vers/tomcat/webapps bash deployTestApplication.sh"
  exit 1
fi

WAR_FILE="$BUILD_DIR/$APP_NAME.war"
TARGET_WAR="$TOMCAT_WEBAPPS/$APP_NAME.war"

if ! cp -f "$WAR_FILE" "$TARGET_WAR" 2>/dev/null; then
  echo "Copie normale refusee, tentative avec sudo..."
  if ! sudo cp -f "$WAR_FILE" "$TARGET_WAR"; then
    echo "Erreur: impossible de copier $WAR_FILE vers $TARGET_WAR"
    echo "Lance cette commande pour déployer:"
    echo "sudo cp -f \"$SCRIPT_DIR/$WAR_FILE\" \"$TARGET_WAR\""
    exit 1
  fi
fi

echo "Application déployée dans $TARGET_WAR"
if [ "$APP_NAME" = "ROOT" ]; then
  echo "URL à ouvrir: http://localhost:$TOMCAT_PORT/"
else
  echo "URL à ouvrir: http://localhost:$TOMCAT_PORT/$APP_NAME/"
  echo "Exemple: http://localhost:$TOMCAT_PORT/$APP_NAME/test2"
fi
echo "Si l'ancienne version reste affichee, redemarre ton Tomcat local:"
echo "$DEFAULT_TOMCAT_HOME/bin/shutdown.sh && $DEFAULT_TOMCAT_HOME/bin/startup.sh"
