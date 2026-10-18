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
        assertView(servlet, "GET", "/test4", "Test 4 depuis TestController3");
        assertView(servlet, "GET", "/details", "Details");

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("numero", "4196");
        loginParams.put("nom", "Armando");
        Exchange login = call(servlet, "POST", "/login", loginParams);
        check(login.status == 200, "Statut incorrect pour /login");
        check("text/plain".equals(login.contentType), "Content-Type incorrect pour /login");
        check(login.body.toString().contains("le numero est : 4196"), "Le numero doit etre injecte dans /login");
        check(login.body.toString().contains("le nom est : Armando"), "Le nom doit etre injecte dans /login");

        Map<String, String> userParams = new HashMap<>();
        userParams.put("id", "1");
        userParams.put("name", "mi");
        userParams.put("email", "mi@example.com");
        JsonNode user = json(servlet, "POST", "/param", userParams);
        check(user.get("id").asInt() == 1, "L'id du User doit etre injecte");
        check(user.get("name").asText().equals("mi"), "Le name du User doit etre injecte");
        check(user.get("email").asText().equals("mi@example.com"), "L'email du User doit etre injecte");

        JsonNode personnes = json(servlet, "GET", "/liste");
        check(personnes.get("total").asInt() == 3, "Le total des personnes doit etre 3");
        check(personnes.get("personnes").size() == 3, "La liste doit contenir les personnes de la base");
        check(personnes.get("personnes").get(0).get("nom").asText().equals("Aina"),
            "Les objets Personne doivent etre serialises");
        assertMessage(servlet, "GET", "/init-personnes", "Base initialisee avec 3 personnes");

        check(json(servlet, "GET", "/sprint6-test/spring").asBoolean(), "L'injection Spring doit fonctionner en JSON");

        Exchange view = call(servlet, "GET", "/sprint6-test/view");
        check(view.forwards == 1 && "/view.jsp".equals(view.view), "Une methode sans @Json doit ouvrir sa vue");
        check("Vue du sprint 5".equals(view.attributes.get("message")), "Les donnees doivent passer a la JSP");
        check(view.body.toString().isEmpty() && view.contentType == null, "La vue ne doit pas produire de JSON");

        assertFailure(servlet, "/sprint6-test/invalid", "doit retourner");
        assertFailure(servlet, "/sprint6-test/null-view", "a retourne null");
        Exchange missing = call(servlet, "GET", "/route-inconnue");
        check(missing.status == 404 && missing.forwards == 0, "Une route inconnue doit retourner 404");
        Exchange wrongMethod = call(servlet, "POST", "/liste");
        check(wrongMethod.status == 404, "Le routage doit respecter la methode HTTP");

        System.out.println("Sprint 6/7 OK : routes GET/POST, JSON UTF-8, ModelAndView, Spring, vues, erreurs et binding de parametres.");
    }

    private static void assertMessage(FrontControllerServlet servlet, String method, String path, String message)
        throws Exception {
        check(json(servlet, method, path).get("message").asText().equals(message), "Message incorrect pour " + method + " " + path);
    }

    private static void assertView(FrontControllerServlet servlet, String method, String path, String message)
        throws Exception {
        Exchange exchange = call(servlet, method, path);
        check(exchange.status == 200, "Statut incorrect pour " + path);
        check(exchange.forwards == 1 && "/view.jsp".equals(exchange.view), "La route doit ouvrir /view.jsp : " + path);
        check(message.equals(exchange.attributes.get("message")), "Message de vue incorrect pour " + path);
        check(exchange.body.toString().isEmpty() && exchange.contentType == null, "La route ne doit pas produire de JSON : " + path);
    }

    private static JsonNode json(FrontControllerServlet servlet, String method, String path) throws Exception {
        return json(servlet, method, path, new HashMap<>());
    }

    private static JsonNode json(FrontControllerServlet servlet, String method, String path, Map<String, String> params) throws Exception {
        Exchange exchange = call(servlet, method, path, params);
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
        return call(servlet, method, path, new HashMap<>());
    }

    private static Exchange call(FrontControllerServlet servlet, String method, String path, Map<String, String> params) throws Exception {
        Exchange exchange = new Exchange();
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getRequestURI": return "/testApplication" + path;
                case "getContextPath": return "/testApplication";
                case "getMethod": return method;
                case "getParameter": return params.get((String) a[0]);
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
