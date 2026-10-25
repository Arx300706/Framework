# Projet Framework

Un seul framework actif : `framework/`. Il produit le JAR utilisé par
`TestApplication/`, l'application de démonstration. Le dépôt Git reste à la racine.

## Organisation

```text
framework/
  src/main/java/com/framework/  annotations, routage, vues, JSON et binding
  lib/                         dépendances du framework, conservées dans Git
  build/                       JAR et classes générés, ignorés par Git
TestApplication/
  src/main/java/com/app/        contrôleurs, modèles et accès aux données
  src/main/webapp/              JSP, HTML et WEB-INF/web.xml
  src/test/java/               tests de routage et de base de données
  lib/                         dépendances propres à l'application (H2)
  sql/                         scripts PostgreSQL existants
  build/                       webapp assemblée et WAR, ignorés par Git
scripts/                       construction, tests et déploiement
docs/sprints/                  consignes et documentation des sprints
docs/reference/                documents de référence existants
docs/archives/                 anciens brouillons, hors construction
```

Les fichiers de `docs/reference/` et `docs/archives/` sont conservés comme
références historiques ; cette page décrit l'organisation actuelle.

## Commandes

Depuis la racine, avec un JDK disponible (`java`, `javac`, `jar`) et Bash :

```bash
bash scripts/build.sh             # construit framework.jar puis testApplication.war
bash scripts/test.sh              # vérifie les sprints et l'accès H2 en mémoire
bash scripts/deploy.sh            # reconstruit puis copie le WAR dans Tomcat
```

Les scripts retrouvent la racine depuis leur propre emplacement : ils peuvent
aussi être appelés par chemin absolu depuis un autre répertoire.
La construction ne télécharge rien, ne modifie pas les sources et ne déploie rien.
Les tests utilisent le framework et l'application construits, avec des bases H2
en mémoire ; ils ne touchent pas la base persistante de l'application.

```bash
bash scripts/build.sh framework   # construit seulement le framework
bash scripts/test.sh sprints      # routage, JSON, vues, Spring et sprints 7/7 bis
bash scripts/test.sh database     # test du repository
TOMCAT_WEBAPPS=/chemin/tomcat/webapps bash scripts/deploy.sh
```

`APP_NAME` permet de changer le nom du WAR (défaut : `testApplication`).
`TOMCAT_WEBAPPS`, `CATALINA_BASE` et `CATALINA_HOME` permettent de choisir Tomcat.
Les chemins Tomcat habituels du projet restent proposés par défaut.
`TOMCAT_PORT` règle le port affiché dans les messages (défaut : `8081`) ; il ne
change pas la configuration du serveur.

Les anciens points d'entrée restent disponibles et appellent les scripts communs :

- `framework/deployFramework.sh` : construit le JAR du framework.
- `TestApplication/deployTestApplication.sh` : construit puis déploie l'application.
- `TestApplication/runSprint6Tests.sh` : lance les tests des sprints.
- `TestApplication/runBasicTests.sh` : lance le test de base de données.

Le JAR et les dépendances sont assemblés dans `TestApplication/build/webapp/WEB-INF/lib`.
Il n'est plus nécessaire de les copier dans les sources web. L'API Servlet est
utilisée pour compiler, puis fournie par Tomcat ; elle n'est pas embarquée dans le WAR.
Les options `-parameters` sont conservées pour l'injection par nom des arguments.

## Fonctionnalités conservées

- Contrôleurs annotés et routage par URL et méthode GET/POST.
- Invocation par réflexion et initialisation des routes par le listener.
- `ModelAndView` et transfert vers les JSP.
- Réponses JSON avec `@Json`, `@RestApi` et `@ApiRest`.
- Paramètres simples (`/login`) et construction d'un `User` (`/param`).
- Liste des personnes, repository JDBC et dépendances Spring existantes.
- Classe `MapFile` et scripts SQL conservés sans changement.

Après déploiement, `/details` affiche les deux formulaires. `/test1` et `/liste`
renvoient du JSON. La configuration de base de données reste dans
`TestApplication/src/main/java/com/app/util/DatabaseConnection.java`.

`MapFile` est une classe existante : le traitement multipart des uploads reste à
implémenter. Les scripts PostgreSQL sont conservés, mais les tests automatiques
couvrent H2 ; ils ne valident pas un serveur PostgreSQL.

## Git et sauvegarde

Le dépôt, sa branche et son adresse distante restent les mêmes. Le rangement ne
nécessite ni recréation du dépôt ni réécriture de l'historique.
Les sources, scripts, documents et dépendances nécessaires sont suivis ; les
produits de compilation sont ignorés et retirés de l'index Git.

Une sauvegarde complète avant rangement, incluant l'ancien `Framework/`, les
modifications locales et `.git/`, se trouve dans
`.local-backups/avant-rangement-20261016-085223.tar.gz` (non versionnée).
Le fichier JSON voisin contient l'état Git et les empreintes des sources avant rangement.
L'ancien `Framework/` reste également consultable dans l'historique Git.
