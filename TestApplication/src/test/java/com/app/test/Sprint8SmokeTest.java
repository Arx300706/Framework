package com.app.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.core.FrontControllerListner;
import com.framework.core.FrontControllerServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Vérifie le binding à travers le routage public, y compris le contenu binaire. */
public class Sprint8SmokeTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final byte[] CONTENT = {0, 1, 13, 10, (byte) 128, (byte) 255};

    public static void main(String[] args) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        ServletContext context = proxy(ServletContext.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getAttribute": return attributes.get(a[0]);
                case "setAttribute": attributes.put((String) a[0], a[1]); return null;
                case "getInitParameter": return "com.app.controller";
                default: return null;
            }
        });
        new FrontControllerListner().contextInitialized(new ServletContextEvent(context));
        FrontControllerServlet servlet = new FrontControllerServlet();
        servlet.init(proxy(ServletConfig.class, (p, m, a) ->
            m.getName().equals("getServletContext") ? context : null));

        Upload file = new Upload("C:\\fakepath\\portrait.png", CONTENT);
        JsonNode employe = json(call(servlet, "/employe/save", Map.of("nom", "Élodie"), file, true, null));
        check(employe.get("nom").asText().equals("Élodie"), "Le champ texte doit être injecté");
        assertFile(employe.get("photo"), "portrait.png", "png", CONTENT);
        check(file.closed, "Le flux du fichier doit être fermé");

        JsonNode absent = json(call(servlet, "/employe/save", Map.of("nom", "Sans photo"), null, true, null));
        check(absent.get("photo").isNull(), "Un fichier absent doit donner null");
        JsonNode unselected = json(call(servlet, "/employe/save", Map.of(), new Upload("", new byte[0]), true, null));
        check(unselected.get("photo").isNull(), "Une sélection vide doit donner null");
        JsonNode textPart = json(call(servlet, "/employe/save", Map.of(), new Upload(null, CONTENT), true, null));
        check(textPart.get("photo").isNull(), "Une partie texte ne doit pas devenir un fichier");
        JsonNode normal = json(call(servlet, "/employe/save", Map.of("nom", "Normal"), null, false, null));
        check(normal.get("photo").isNull(), "Le binding sans multipart doit rester possible");

        JsonNode empty = json(call(servlet, "/sprint8-test/direct", Map.of(), new Upload("empty.txt", new byte[0]), true, null));
        assertFile(empty, "empty.txt", "txt", new byte[0]);
        JsonNode noExtension = json(call(servlet, "/sprint8-test/direct", Map.of(), new Upload("../../README", CONTENT), true, null));
        assertFile(noExtension, "README", "", CONTENT);
        JsonNode hidden = json(call(servlet, "/sprint8-test/direct", Map.of(), new Upload(".photo", CONTENT), true, null));
        assertFile(hidden, ".photo", "", CONTENT);
        JsonNode legacy = json(call(servlet, "/sprint8-test/legacy", Map.of(), new Upload("archive.tar.gz", CONTENT), true, null));
        assertFile(legacy, "archive.tar.gz", "gz", CONTENT);

        JsonNode constructed = json(call(servlet, "/sprint8-test/constructor",
            Map.of("nom", "Armando", "numero", "4196"), new Upload("photo.jpg", CONTENT), true, null));
        check(constructed.get("numero").asInt() == 4196, "La conversion numérique doit être conservée");
        check(constructed.get("nom").asText().equals("Armando"), "Le constructeur reçoit le texte");
        assertFile(constructed.get("photo"), "photo.jpg", "jpg", CONTENT);

        Exchange large = call(servlet, "/employe/save", Map.of(), null, true, new IllegalStateException("trop grand"));
        check(large.status == 413, "Un dépassement de taille doit retourner HTTP 413");
        Exchange malformed = call(servlet, "/employe/save", Map.of(), null, true, new ServletException("boundary absente"));
        check(malformed.status == 400, "Un multipart invalide doit retourner HTTP 400");
        System.out.println("Sprint 8 OK : champs et constructeurs, argument direct, compatibilite MapFile, octets, flux ferme, fichiers absents/vides, erreurs multipart.");
    }

    private static Exchange call(FrontControllerServlet servlet, String path, Map<String, String> params,
                                 Upload upload, boolean multipart, Exception parseFailure) throws Exception {
        Exchange exchange = new Exchange();
        boolean[] parsed = {false};
        HttpServletRequest request = proxy(HttpServletRequest.class, (p, m, a) -> {
            switch (m.getName()) {
                case "getRequestURI": return "/testApplication" + path;
                case "getContextPath": return "/testApplication";
                case "getMethod": return "POST";
                case "getContentType": return multipart ? "multipart/form-data; boundary=test" : "application/x-www-form-urlencoded";
                case "getParts":
                    check(multipart, "getParts ne doit pas être appelé sans multipart");
                    if (parseFailure != null) throw parseFailure;
                    parsed[0] = true;
                    return upload == null ? java.util.List.of() : java.util.List.of(upload.part);
                case "getPart":
                    check(multipart && parsed[0], "Le multipart doit être parsé avant le binding");
                    return "photo".equals(a[0]) && upload != null ? upload.part : null;
                case "getParameter":
                    check(!multipart || parsed[0], "Le parsing précède la lecture des paramètres texte");
                    return params.get(a[0]);
                default: return null;
            }
        });
        HttpServletResponse response = proxy(HttpServletResponse.class, (p, m, a) -> {
            switch (m.getName()) {
                case "setStatus": case "sendError": exchange.status = (Integer) a[0]; return null;
                case "setContentType": exchange.contentType = (String) a[0]; return null;
                case "setCharacterEncoding": exchange.encoding = (String) a[0]; return null;
                case "getWriter": return new PrintWriter(exchange.body);
                default: return null;
            }
        });
        servlet.doPost(request, response);
        return exchange;
    }

    private static JsonNode json(Exchange exchange) throws Exception {
        check(exchange.status == 200, "HTTP 200 attendu");
        check("application/json".equals(exchange.contentType), "Une réponse JSON est attendue");
        check("UTF-8".equals(exchange.encoding), "UTF-8 attendu");
        return JSON.readTree(exchange.body.toString());
    }

    private static void assertFile(JsonNode file, String name, String extension, byte[] expected) throws Exception {
        check(file.get("nom").asText().equals(name), "Nom de fichier incorrect");
        check(file.get("extension").asText().equals(extension), "Extension incorrecte");
        check(Arrays.equals(expected, file.get("contenu").binaryValue()), "Les octets doivent être conservés exactement");
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static class Upload {
        boolean closed;
        final Part part;

        Upload(String name, byte[] data) {
            part = proxy(Part.class, (p, m, a) -> {
                switch (m.getName()) {
                    case "getName": return "photo";
                    case "getSubmittedFileName": return name;
                    case "getSize": return (long) data.length;
                    case "getInputStream": return new ByteArrayInputStream(data) {
                        @Override public void close() throws IOException {
                            closed = true;
                            super.close();
                        }
                    };
                    default: return null;
                }
            });
        }
    }

    private static class Exchange {
        int status = 200;
        String contentType;
        String encoding;
        final StringWriter body = new StringWriter();
    }
}
