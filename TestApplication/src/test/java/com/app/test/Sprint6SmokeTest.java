package com.app.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.FrontControllerListner;
import com.framework.core.FrontControllerServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.context.WebApplicationContext;

/** Teste le routage complet avec des requetes Servlet simulees et une base H2 en memoire. */
public class Sprint6SmokeTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Map<String, Object> contextAttributes = new HashMap<>();
        contextAttributes.put(FrontControllerListner.SPRING_ROOT,
            proxy(WebApplicationContext.class, (p, m, a) -> null));

        ServletContext context = proxy(ServletContext.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getAttribute": return contextAttributes.get(a[0]);
                case "setAttribute": contextAttributes.put((String) a[0], a[1]); return null;
                case "getInitParameter": return "com.app.controller";
                default: return null;
            }
        });
        new FrontControllerListner().contextInitialized(new ServletContextEvent(context));

        FrontControllerServlet servlet = new FrontControllerServlet();
        servlet.init(proxy(ServletConfig.class, (p, m, a) ->
            m.getName().equals("getServletContext") ? context : null));

        assertMessage(servlet, "GET", "/test1", "GET test1");
        assertMessage(servlet, "POST", "/test1", "POST test1");
        assertMessage(servlet, "GET", "/accueil", "Bienvenue");
        assertMessage(servlet, "GET", "/test3", "Test 3 depuis TestController2");
        assertMessage(servlet, "GET", "/test4", "Test 4 depuis TestController3");
        assertMessage(servlet, "GET", "/details", "Details");

        JsonNode personnes = json(servlet, "GET", "/liste");
        check(personnes.get("total").asInt() == 3, "Le total des personnes doit etre 3");
        check(personnes.get("personnes").size() == 3, "La liste doit contenir les personnes de la base");
        check(personnes.get("personnes").get(0).get("nom").asText().equals("Aina"),
            "Les objets Personne doivent etre serialises");
        assertMessage(servlet, "GET", "/init-personnes", "Base initialisee avec 3 personnes");

        JsonNode personne = json(servlet, "GET", "/sprint6-test/object");
        check(personne.get("id").asInt() == 7 && personne.get("age").asInt() == 21,
            "Un objet doit conserver ses proprietes");
        check(personne.get("nom").asText().equals("Aïna \"test\"\nAntananarivo"),
            "Les accents, guillemets et sauts de ligne doivent etre conserves");
        check(json(servlet, "GET", "/sprint6-test/list").size() == 2, "Une liste doit devenir un tableau JSON");
        JsonNode texte = json(servlet, "GET", "/sprint6-test/string");
        check(texte.isTextual() && texte.asText().equals("Bonjour \"été\"\nC:\\test\t!"),
            "Une chaine doit etre encodee en JSON avec les caracteres echappes");
        check(json(servlet, "GET", "/sprint6-test/number").asInt() == 42, "Un nombre doit rester un nombre");
        check(json(servlet, "GET", "/sprint6-test/boolean").asBoolean(), "Un booleen doit rester un booleen");
        JsonNode tableau = json(servlet, "GET", "/sprint6-test/array");
        check(tableau.isArray() && tableau.size() == 3 && tableau.get(2).asInt() == 3,
            "Un tableau Java doit devenir un tableau JSON");
        check(json(servlet, "GET", "/sprint6-test/null").isNull(), "Un retour null doit produire null en JSON");

        JsonNode data = json(servlet, "GET", "/sprint6-test/data");
        check(data.has("personne") && data.has("absent") && data.get("absent").isNull(),
            "Les donnees du ModelAndView doivent etre serialisees meme sans vue");
        check(!data.has("view") && !data.has("data"), "Le JSON ne doit pas inclure la vue ou envelopper les donnees");
        check(json(servlet, "GET", "/sprint6-test/spring").asBoolean(), "L'injection Spring doit fonctionner en JSON");

        Exchange view = call(servlet, "GET", "/sprint6-test/view");
        check(view.forwards == 1 && "/view.jsp".equals(view.view), "Une methode sans @RestApi doit ouvrir sa vue");
        check("Vue du sprint 5".equals(view.attributes.get("message")), "Les donnees doivent passer a la JSP");
        check(view.body.toString().isEmpty() && view.contentType == null, "La vue ne doit pas produire de JSON");

        assertFailure(servlet, "/sprint6-test/invalid", "doit retourner");
        assertFailure(servlet, "/sprint6-test/null-view", "a retourne null");
        Exchange missing = call(servlet, "GET", "/route-inconnue");
        check(missing.status == 404 && missing.forwards == 0, "Une route inconnue doit retourner 404");
        Exchange wrongMethod = call(servlet, "POST", "/liste");
        check(wrongMethod.status == 404, "Le routage doit respecter la methode HTTP");

        System.out.println("Sprint 6 OK : routes GET/POST, JSON UTF-8, objets/listes/valeurs/null, base H2, Spring, vues et erreurs.");
    }

    private static void assertMessage(FrontControllerServlet servlet, String method, String path, String message)
        throws Exception {
        check(json(servlet, method, path).get("message").asText().equals(message), "Message incorrect pour " + method + " " + path);
    }

    private static JsonNode json(FrontControllerServlet servlet, String method, String path) throws Exception {
        Exchange exchange = call(servlet, method, path);
        check(exchange.status == 200, "Statut incorrect pour " + path);
        check("application/json".equals(exchange.contentType), "Content-Type incorrect pour " + path);
        check("UTF-8".equals(exchange.encoding) && !exchange.writerBeforeHeaders, "Encodage incorrect pour " + path);
        check(exchange.forwards == 0 && exchange.view == null, "Une route JSON ne doit pas demander de vue : " + path);
        check(exchange.attributes.isEmpty(), "Une route JSON ne doit pas remplir les attributs d'une vue : " + path);
        JsonNode result = JSON.readTree(exchange.body.toString());
        check(result != null, "Le corps JSON ne doit pas etre vide pour " + path);
        return result;
    }

    private static Exchange call(FrontControllerServlet servlet, String method, String path) throws Exception {
        Exchange exchange = new Exchange();
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getRequestURI": return "/testApplication" + path;
                case "getContextPath": return "/testApplication";
                case "getMethod": return method;
                case "setAttribute": exchange.attributes.put((String) a[0], a[1]); return null;
                case "getRequestDispatcher":
                    exchange.view = (String) a[0];
                    return proxy(RequestDispatcher.class, (d, dm, da) -> {
                        if (dm.getName().equals("forward")) exchange.forwards++;
                        return null;
                    });
                default: return null;
            }
        });
        HttpServletResponse response = proxy(HttpServletResponse.class, (p, m, a) -> {
            switch (m.getName()) {
                case "setStatus": exchange.status = (Integer) a[0]; return null;
                case "setContentType": exchange.contentType = (String) a[0]; return null;
                case "setCharacterEncoding": exchange.encoding = (String) a[0]; return null;
                case "getWriter":
                    exchange.writerBeforeHeaders = exchange.contentType == null || exchange.encoding == null;
                    return new PrintWriter(exchange.body);
                default: return null;
            }
        });
        if (method.equals("POST")) servlet.doPost(request, response);
        else servlet.doGet(request, response);
        return exchange;
    }

    private static void assertFailure(FrontControllerServlet servlet, String path, String expected) throws Exception {
        try {
            call(servlet, "GET", path);
            throw new AssertionError("Une erreur est attendue pour " + path);
        } catch (ServletException e) {
            check(e.getMessage().contains(expected), "Erreur inattendue pour " + path + ": " + e.getMessage());
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static class Exchange {
        final StringWriter body = new StringWriter();
        final Map<String, Object> attributes = new HashMap<>();
        int status = 200;
        int forwards;
        String contentType;
        String encoding;
        String view;
        boolean writerBeforeHeaders;
    }
}
