## Sprint 0 : Front controller
- creation de FrontController
- creation du fonction processRequest
- apple de process request avec doGet et doPost

## Sprint 1 : Annotation
- creation de l'annotation "Controller"
- creation des classes utils :
    - UtilAnalyser : fonction chercher tous les classes dans le projet (selon package ou non)
    - ScanAnnotation : fonction trouver tous les classes qui contient les annotations à chercher
- ajout de l'attribut classes dans FrontController
- Overide de la fonction init() pour charger tous les classes lors du lancement
- Modification de processRequest pour afficher tous les classes trouver

##  Sprint 2 : Url Mapping
- creation de l'annotation "UrlMaping"
- ajout de l'argument url dans l'annotation
- Ajout de fonction dans ScanAnnotation :
    - fonction getMethodWithAnnotation : fonction qui retourne tous les methodes d'une classe qui contient une annotation
- creation d'un classe Mapping qui contient class et method 
- ajout d'un attribut HashMap mapping dans FrontController :
    - clé : url de la map (String)
    - value : la nouvelle classe Mapping
- creation d'une methode initMapping dans FrontController pour remplir le hashMap
    - parcour des listes de classes initialiser
    - appel de la fonction getMethodWithAnnotation
    - creation des mapping 
- appel de initMapping dans init
- changement de processRequest 
    - afficher une erreur en cas de mapping introuvable et afficher tous les url disponible
    - afficher l'url, sa classe et methode correspondant si url dispo
- Refactor : creation des variables static final pour garder les class des annotation


## Sprint 3 

### Sprint 3 : suite de Sprint 2
Prise en charge des types de methode http (GET/POST)
- Creation d'une classe UrlMethode :
    - attributs :
        - url (String)
        - methode (String ou creation d'un Enum java) GET/POST
    - Override de la fonction equals pour des futurs comparaison
- Ajout d'un nouveau attribut dans l'annotation UrlMapping : method pour definir le methode http
- Changement de la clé de l'ancien map par la nouvelle classe
- Mise à jour de la fonction initMApping

### Sprint 3-Bis
- Invocation de la methode qui contient le l'url par reflexion

### Sprint 4-Special
- Creation de la classe FrontControllerListner.java :
    - implement : ServletContextListner
    - Override de la fonction ContextInitialized
    - transfert des fonctions de l'ancien init pour initialiser 
    - envoye du mapping gener par ServletContext
    
### Sprint 5-Support de vue ModelAndView
- Creation de la classe ModelAndView :
    - attributs : String view, Map<String, Object> data
    - fonction addAttribut(clé, object) pour ajouter des élements à la map
    - constructeur avec le parametre view
    - fonction setview
- Modification de ProcessRequest sur l'invocation:
    - exception si le type de retour n'est pas ModelAndView
    - caster le resultat de retour 
    - creation d'un fonction pour ajouter data du model dans le request SErvlet : addArgToRequest(req, map)
    - creation d'un dispatcher qui envoye vers la page correspondant au vu
lorsque on appelle les url du code mais direct  il retournlorsque on appelle les url du code mais direct  il retourn json(anotation niveau methode)
lorsque on appelle les url du code mais direct  il retourn json(anotation niveau methode)
 json(anotation niveau methode)


### Sprint 5 bis
-trouver un moyen de prendre venant de la base  
-prendre la liste dans la base par exmple liste personne dia on prend les personnes dans la base 

### Sprint 6 
-ts mandeh any view rehef antsohina ilay url simuler @ilay code fa mamerina json
ne plus aller vers les view lorsque on appelle les url du code mais direct  il retourn json(anotation niveau methode)

### sprint 7 
but: anaty controller an'ilay application afaka manana fonction misy argugments amzay
du genre:
@GetMapping(value"/login",method="POST")
public String loginPost(Integer numero ,String nom){
    System.out.println("Login method POST appelee !" + numero + "et le nom est : " + nom);
    return "LoginPost method POST appele !" + "le numero est :" + numero + " et le nom est :" + nom;
}
et met le formulaire de post dans ModelAndView.jsp

<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Model and View</title>
</head>
<body>

<div class="container">

    <table>
        <thead>
            <tr>
                <th>Clé</th>
                <th>Valeur</th>
            </tr>
        </thead>

        <tbody>
            <tr>
                <td>key1</td>
                <td>${key1}</td>
            </tr>

            <tr>
                <td>key2</td>
                <td>${key2}</td>
            </tr>
        </tbody>
    </table>

    <h1>Faire un post :</h1>

    <form
        action="${pageContext.request.contextPath}/login"
        method="POST"
        style="margin-top: 15px;"
    >

        <div style="margin-bottom: 12px;">
            <label for="numero">Numéro :</label><br>

            <input
                type="number"
                id="numero"
                name="numero"
                value="4196"
                required
                style="padding: 8px; width: 200px; margin-top: 4px;"
            >
        </div>

        <div style="margin-bottom: 12px;">
            <label for="nom">Nom :</label><br>

            <input
                type="text"
                id="nom"
                name="nom"
                value="Armando"
                required
                style="padding: 8px; width: 200px; margin-top: 4px;"
            >
        </div>

        <button
            type="submit"
            style="background-color: #007bff;
                   color: white;
                   border: none;
                   padding: 10px 20px;
                   border-radius: 4px;
                   cursor: pointer;"
        >
            Envoyer (POST)
        </button>

    </form>

</div>

</body>
</html>
dia io formulaire post io mandefa parametre anatty url Pour moi mes paramettre numero 4196 et nom = Armando

Dia anaty frontcontrollerservlet amzay 
Maka an'ilay anaranle paramètres ao anaty url (numero, nom ) sy ny value ( 4196,Armando) 
Dia comparer-na am arguments anle fonction ( numero, nom ) 
Dia refa mitovy Ilay anarany 
Dia caster-na int Ilay 4196 , caster-na string Ilay Armando
Dia refa sendra tsy nandefa an'ilay arguments anankiray Ilay url dia atao null fotsiny

## et pour le 7 bis c'est a peu pres pareil mais juste 

@GetMapping(value = "/param", method = "POST")
@ApiRest
public User testParametresObject(User user) {

    System.out.println(
        "Le numéro est : " + user.getId()
        + " et le nom est : " + user.getName()
        + " et l'email est : " + user.getEmail()
    );

    return new User(
        user.getId(),
        user.getName(),
        user.getEmail()
    );
}
 et puis dans user.java 


package com.app.model;

public class User {

    private final int id;
    private final String name;
    private final String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User() {
        this.id = 0;
        this.name = "";
        this.email = "";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
et j'ai utilise ce formulaire 

<h1>Faire un post (objet User) :</h1>

<form
    action="${pageContext.request.contextPath}/param"
    method="POST"
    style="margin-top: 15px;"
>

    <div style="margin-bottom: 12px;">
        <label for="id">Id :</label><br>

        <input
            type="number"
            id="id"
            name="id"
            value="1"
            required
            style="padding: 8px; width: 200px; margin-top: 4px;"
        >
    </div>

    <div style="margin-bottom: 12px;">
        <label for="name">Name :</label><br>

        <input
            type="text"
            id="name"
            name="name"
            value="mi"
            required
            style="padding: 8px; width: 200px; margin-top: 4px;"
        >
    </div>

    <div style="margin-bottom: 12px;">
        <label for="email">Email :</label><br>

        <input
            type="email"
            id="email"
            name="email"
            value="mi@example.com"
            required
            style="padding: 8px; width: 200px; margin-top: 4px;"
        >
    </div>

    <button
        type="submit"
        style="background-color: #007bff;
               color: white;
               border: none;
               padding: 10px 20px;
               border-radius: 4px;
               cursor: pointer;"
    >
        Envoyer (POST)
    </button>

</form>

## sprint 8 
peut tu me creer des uploads
mamorona class mapflile est les attribut nom(asina ilay fichier uploadena),extension,tableau de bit ohtra argemt ilay fonction employe dia ilay employe manana attribut de type mapfile dia mitovy manao an'ilay bind