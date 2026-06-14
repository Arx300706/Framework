#!/bin/bash

APP_NAME="${APP_NAME:-framework}"
SRC_DIR="${SRC_DIR:-src/main/java}"
WEB_DIR="${WEB_DIR:-src/main/webapp}"
BUILD_DIR="${BUILD_DIR:-build}"
LIB_DIR="${LIB_DIR:-lib}"
SERVLET_API_JAR="${SERVLET_API_JAR:-$LIB_DIR/servlet-api.jar}"
APP_TEST_WEBAPPS="${APP_TEST_WEBAPPS:-/home/armando/Documents/S3_S4/S4/Mr Naina/PROJET_FRAMEWORK/PROJET_FRAMEWORK (copie)/TestApplication/src/main/webapp}"

# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes
mkdir -p $BUILD_DIR/lib

# Compilation des fichiers Java avec le JAR des Servlets
find $SRC_DIR -name "*.java" > sources.txt
javac -cp ".:lib/*" -d $BUILD_DIR/classes @sources.txt 
rm sources.txt

cp -r $LIB_DIR/*.jar $BUILD_DIR/lib/

# Générer le fichier .jar 
cd $BUILD_DIR || exit

jar -cvf $APP_NAME.jar -C classes . -C lib .
cd ..

# Déploiement dans le lib
# s'assurer que le dossier lib de l'application de test existe
mkdir -p $APP_TEST_WEBAPPS/WEB-INF/lib
mkdir -p "$APP_TEST_WEBAPPS"/WEB-INF/lib
cp -f "$BUILD_DIR"/"$APP_NAME".jar "$APP_TEST_WEBAPPS"/WEB-INF/lib
echo "" 