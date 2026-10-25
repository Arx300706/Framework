package com.app.test;

import com.framework.core.FrontControllerListner;
import com.framework.core.FrontControllerServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Vérifie la session framework : une instance par client, injectée dans l'action. */
public class Sprint9SmokeTest {
    public static void main(String[] args) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        ServletContext context = proxy(ServletContext.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getAttribute": return attributes.get(a[0]);
                case "setAttribute": attributes.put((String) a[0], a[1]); return null;
                case "getInitParameter": return "controller-package".equals(a[0]) ? "com.app.controller" : null;
                default: return null;
            }
        });
        new FrontControllerListner().contextInitialized(new ServletContextEvent(context));
        FrontControllerServlet servlet = new FrontControllerServlet();
        servlet.init(proxy(ServletConfig.class, (p, m, a) ->
            m.getName().equals("getServletContext") ? context : null));

        Client alice = new Client();
        Client bob = new Client();

        check(call(servlet, alice, "GET", "/sprint9-test/set", Map.of("valeur", "A")).equals("A"),
            "L'action doit pouvoir écrire dans la session");
        check(alice.cookie != null, "Un cookie de session doit être émis à la première requête");
        check(call(servlet, alice, "GET", "/sprint9-test/get", Map.of()).equals("A"),
            "Le même client doit retrouver sa session via le cookie");

        check(call(servlet, bob, "GET", "/sprint9-test/get", Map.of()).equals("null"),
            "Un autre client ne doit pas voir la session d'Alice");
        check(bob.cookie == null || !bob.cookie.getValue().equals(alice.cookie.getValue()),
            "Chaque client doit avoir sa propre session");
        call(servlet, bob, "GET", "/sprint9-test/set", Map.of("valeur", "B"));
        check(call(servlet, alice, "GET", "/sprint9-test/get", Map.of()).equals("A"),
            "L'écriture de Bob ne doit pas modifier la session d'Alice");

        check(call(servlet, alice, "GET", "/sprint9-test/remove", Map.of()).equals("null"),
            "La suppression doit retirer l'attribut");
        check(call(servlet, alice, "GET", "/sprint9-test/get", Map.of()).equals("null"),
            "Après suppression l'attribut doit être absent");

        call(servlet, alice, "GET", "/sprint9-test/set", Map.of("valeur", "C"));
        String ancienCookie = alice.cookie.getValue();
        check(call(servlet, alice, "GET", "/sprint9-test/invalidate", Map.of()).equals("invalidee"),
            "L'action d'invalidation doit s'exécuter");
        check(call(servlet, alice, "GET", "/sprint9-test/get", Map.of()).equals("null"),
            "Une session invalidée ne doit plus rien contenir");
        check(!alice.cookie.getValue().equals(ancienCookie),
            "Après invalidation un nouveau cookie de session doit être émis");

        System.out.println("Sprint 9 OK : session injectée dans l'action, une session par client, suppression et invalidation.");
    }

    private static String call(FrontControllerServlet servlet, Client client, String method,
                               String path, Map<String, String> params) throws Exception {
        StringWriter body = new StringWriter();
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getRequestURI": return "/testApplication" + path;
                case "getContextPath": return "/testApplication";
                case "getMethod": return method;
                case "getParameter": return params.get(a[0]);
                case "getCookies": return client.cookie == null ? null : new Cookie[] {client.cookie};
                default: return null;
            }
        });
        HttpServletResponse response = proxy(HttpServletResponse.class, (p, m, a) -> {
            switch (m.getName()) {
                case "addCookie": client.cookie = (Cookie) a[0]; return null;
                case "getWriter": return new PrintWriter(body);
                default: return null;
            }
        });
        if (method.equals("GET")) {
            servlet.doGet(request, response);
        } else {
            servlet.doPost(request, response);
        }
        return body.toString().replace("\"", "");
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static class Client {
        Cookie cookie;
    }
}
