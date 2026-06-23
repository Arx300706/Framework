| Élément              | Framework (JAR)    | Application (WAR)    |
| -------------------- | ------------------ | -------------------- |
| `pom.xml`            | build du framework | build de l’app       |
| packaging            | `jar`              | `war`                |
| contient web.xml     | NON              | OUI                |
| contient servlet     | OUI              | NON (juste config) |
| contient controllers | NON              | OUI                |



## Vérifier ce que contient un .jar

jar -tf framework.jar

jar -tf testApplication.war