# Sprint 6 : retour JSON avec une annotation de methode

L'annotation `com.framework.annotation.RestApi` transforme le resultat d'une methode en JSON, avec `Content-Type: application/json` et l'encodage UTF-8. Le front controller n'ouvre aucune vue pour une methode annotee.

```java
import com.framework.annotation.RestApi;
import com.framework.annotation.urlMapping;

@urlMapping("/personne")
@RestApi
public Personne personne() {
    return new Personne(1, "Aina", 21);
}
```

Le corps de la reponse est :

```json
{"id":1,"nom":"Aina","age":21}
```

Les objets Java, listes, tableaux, maps, chaines, nombres et booleens sont serialises avec Jackson. Un retour `null` produit le JSON `null`. Si la methode retourne un `ModelAndView`, seules ses donnees (`getData()`) sont envoyees ; sa vue n'est pas utilisee. Sans `@RestApi`, la methode doit toujours retourner un `ModelAndView` pour afficher une JSP.

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

Depuis la racine du projet, reconstruire le framework puis deployer l'application :

```bash
bash framework/deployFramework.sh
bash TestApplication/deployTestApplication.sh
```

Le dossier actif est `framework/` (en minuscules). Les dependances JSON sont fournies dans `framework/lib/` : `jackson-databind-2.21.4.jar`, `jackson-core-2.21.4.jar` et `jackson-annotations-2.21.jar`. Le script du framework les copie dans les bibliotheques de l'application avant la construction du WAR.

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

Ces tests recompilent les sources, decouvrent les routes avec le listener et simulent les requetes Servlet. Ils verifient les routes GET/POST, les entetes JSON, l'echappement des caracteres et les accents, la serialisation des objets/listes/valeurs/null, les donnees H2 en memoire, l'injection Spring, le maintien des vues sans annotation et les erreurs de routage. Les controleurs de test sont dans `src/test/java` et ne sont pas inclus dans le WAR.
