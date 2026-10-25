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

public class Sprint10SmokeTest {
    public static void main(String[] args) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        ServletContext context = proxy(ServletContext.class, (p, m, a) -> {
            if (m.getName().equals("getAttribute")) return attributes.get(a[0]);
            if (m.getName().equals("setAttribute")) { attributes.put((String) a[0], a[1]); return null; }
            if (m.getName().equals("getInitParameter")) return "controller-package".equals(a[0]) ? "com.app.controller" : null;
            return null;
        });
        new FrontControllerListner().contextInitialized(new ServletContextEvent(context));
        FrontControllerServlet servlet = new FrontControllerServlet();
        servlet.init(proxy(ServletConfig.class, (p, m, a) -> m.getName().equals("getServletContext") ? context : null));

        Client client = new Client();
        check(call(servlet, client, "/sprint10-test/protege", Map.of()).status == 401,
            "Une action protégée doit refuser un client non authentifié");
        call(servlet, client, "/sprint10-test/login", Map.of("role", "user"));
        check(call(servlet, client, "/sprint10-test/protege", Map.of()).status == 200,
            "Un client authentifié doit accéder à une action protégée sans rôle");
        check(call(servlet, client, "/sprint10-test/admin", Map.of()).status == 403,
            "Un utilisateur sans rôle admin doit être refusé");
        call(servlet, client, "/sprint10-test/login", Map.of("role", "admin"));
        Result admin = call(servlet, client, "/sprint10-test/admin", Map.of());
        check(admin.status == 200 && admin.body.contains("admin"), "Le rôle admin doit être autorisé");
        System.out.println("Sprint 10 OK : authentification (401) et autorisation par rôle (403).");
    }

    private static Result call(FrontControllerServlet servlet, Client client, String path,
                               Map<String, String> params) throws Exception {
        StringWriter body = new StringWriter();
        int[] status = {200};
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getRequestURI": return "/testApplication" + path;
                case "getContextPath": return "/testApplication";
                case "getMethod": return "GET";
                case "getParameter": return params.get(a[0]);
                case "getCookies": return client.cookie == null ? null : new Cookie[] {client.cookie};
                default: return null;
            }
        });
        HttpServletResponse response = proxy(HttpServletResponse.class, (p, m, a) -> {
            switch (m.getName()) {
                case "addCookie": client.cookie = (Cookie) a[0]; return null;
                case "getWriter": return new PrintWriter(body);
                case "setStatus": status[0] = (Integer) a[0]; return null;
                case "sendError": status[0] = (Integer) a[0]; return null;
                default: return null;
            }
        });
        servlet.doGet(request, response);
        return new Result(status[0], body.toString());
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Client { Cookie cookie; }
    private static final class Result {
        final int status;
        final String body;
        Result(int status, String body) { this.status = status; this.body = body; }
    }
}
