# Sprint 6 : retour JSON avec une annotation de methode

L'annotation `com.framework.annotation.RestApi` transforme le resultat d'une methode en JSON, avec `Content-Type: application/json` et l'encodage UTF-8. Le front controller n'ouvre aucune vue pour une methode annotee.

Dans ce projet, les methodes des controleurs retournent surtout un `ModelAndView`. Si la methode porte `@RestApi`, le framework envoie seulement ses donnees (`getData()`) en JSON et n'utilise pas sa vue. Sans `@RestApi`, la methode doit toujours retourner un `ModelAndView` pour afficher une JSP.

Toutes les methodes de demonstration existantes portent maintenant `@RestApi` :

| Methode HTTP | URL | Reponse |
| --- | --- | --- |
| GET | `/test1` | `{"message":"GET test1"}` |
| POST | `/test1` | `{"message":"POST test1"}` |
| GET | `/accueil` | `{"message":"Bienvenue"}` |
| GET | `/test3` | Message du controleur 2 |
| GET | `/test4` | Message du controleur 3 |
| GET | `/details` | `{"message":"Details"}` |
| GET | `/liste` | Message, liste des personnes de la base et total |
| GET | `/init-personnes` | Message d'initialisation de la base |

Par exemple, ouvrir `http://localhost:8081/testApplication/test1` : la reponse est `{"message":"GET test1"}` avec `Content-Type: application/json;charset=UTF-8`, sans forward vers `/view.jsp`.

Depuis la racine du projet, reconstruire le framework puis deployer l'application :

```bash
bash framework/deployFramework.sh
bash TestApplication/deployTestApplication.sh
```

Le dossier actif est `framework/` (en minuscules). Les dependances JSON sont fournies dans `framework/lib/` : `jackson-databind-2.21.4.jar`, `jackson-core-2.21.4.jar` et `jackson-annotations-2.21.jar`. Le script de construction les assemble dans `TestApplication/build/webapp/WEB-INF/lib` avant de produire le WAR.

Avec le nom et le port Tomcat par defaut du script de deploiement :

```bash
curl -i http://localhost:8081/testApplication/test1
curl -i -X POST http://localhost:8081/testApplication/test1
curl -i http://localhost:8081/testApplication/liste
```

Pour lancer les tests sans Tomcat et sans toucher a la base persistante :

```bash
bash TestApplication/runSprint6Tests.sh
```

Ces tests recompilent les sources, decouvrent les routes avec le listener et simulent les requetes Servlet. Ils verifient les routes GET/POST, les entetes JSON, la serialisation des donnees `ModelAndView`, les donnees H2 en memoire, l'injection Spring, le maintien des vues sans annotation et les erreurs de routage. Les routes internes `/sprint6-test/spring`, `/sprint6-test/view`, `/sprint6-test/invalid` et `/sprint6-test/null-view` restent dans `src/test/java` et sont disponibles uniquement avec le script de tests.
