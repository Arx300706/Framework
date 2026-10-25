package com.framework.core;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List ;
import java.util.ArrayList ;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.annotation.ApiRest;
import com.framework.annotation.Autorisation;
import com.framework.annotation.Json;
import com.framework.annotation.RestApi;
import com.framework.mapping.UrlMethode;
import com.framework.session.Session;
import com.framework.session.SessionManager;
import com.framework.upload.FileBinder;
import com.framework.upload.MapFile;

import org.springframework.web.context.WebApplicationContext;


public class FrontControllerServlet extends HttpServlet {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    List<String> listNomController = new ArrayList<>() ;
    Map<UrlMethode, String> urlMappings = new LinkedHashMap<>() ;
    WebApplicationContext springContext;
    SessionManager sessionManager;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        try {
            super.init();

            ServletContext context = getServletContext();
            Object controllersAttribute = context.getAttribute(FrontControllerListner.CONTROLLERS_ATTRIBUTE);
            Object mappingsAttribute = context.getAttribute(FrontControllerListner.URL_MAPPINGS_ATTRIBUTE);
            Object springContextAttribute = context.getAttribute("springContext");
            Object sessionAttribute = context.getAttribute(SessionManager.ATTRIBUTE);

            if (controllersAttribute instanceof List<?>) {
                listNomController = (List<String>) controllersAttribute;
            }

            if (mappingsAttribute instanceof Map<?, ?>) {
                urlMappings = (Map<UrlMethode, String>) mappingsAttribute;
            }

            if (springContextAttribute instanceof WebApplicationContext) {
                springContext = (WebApplicationContext) springContextAttribute;
            }

            if (sessionAttribute instanceof SessionManager) {
                sessionManager = (SessionManager) sessionAttribute;
            }

        } catch (Exception e) {
            e.printStackTrace(); 
            throw new ServletException("Échec de l'initialisation : " + e.getMessage(), e);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
            
        processRequest(req, res);
    }

    public void doPost( HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        
        processRequest(req, res);
    
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException , IOException {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String urlCont = url.substring(contextPath.length());

        if (urlCont == null || urlCont.isEmpty()) {
            urlCont = "/";
        }

        UrlMethode urlMethode = new UrlMethode(urlCont, req.getMethod());
        String mappingCourant = urlMappings.get(urlMethode);

        if (mappingCourant == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().println("Aucune methode trouvee pour " + urlMethode);
            res.getWriter().println("Routes disponibles :");

            for (Map.Entry<UrlMethode, String> mapping : urlMappings.entrySet()) {
                res.getWriter().println(mapping.getKey() + " -> " + mapping.getValue());
            }

            return;
        }

        try {
            if (req.getCharacterEncoding() == null) {
                req.setCharacterEncoding("UTF-8");
            }
            FileBinder.prepare(req);
            Method methode = trouverMethode(mappingCourant);
            if (!autoriser(methode, req, res)) {
                return;
            }
            Object resultat = invoquerMethode(methode, req, res);

            if (isJsonResponse(methode)) {
                writeJson(res, resultat);
            } else if (resultat instanceof ModelAndView) {
                ModelAndView modelAndView = (ModelAndView) resultat;
                addArgToRequest(req, modelAndView.getData());
                dispatch(req, res, modelAndView.getView());
            } else {
                writeText(res, resultat);
            }

        } catch (FileBinder.UploadException e) {
            res.sendError(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la methode : " + e.getMessage(), e);
        }
    }

    private boolean autoriser(Method methode, HttpServletRequest req, HttpServletResponse res) throws IOException {
        Autorisation autorisation = methode.getAnnotation(Autorisation.class);
        if (autorisation == null) {
            autorisation = methode.getDeclaringClass().getAnnotation(Autorisation.class);
        }
        if (autorisation == null) {
            return true;
        }
        if (sessionManager == null) {
            throw new IllegalStateException("Pas de SessionManager dans le ServletContext");
        }

        Session session = sessionManager.sessionCourante(req, res);
        if (!session.estAuthentifie()) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise");
            return false;
        }
        if (!autorisation.role().trim().isEmpty() && !session.aLeRole(autorisation.role())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Role requis : " + autorisation.role());
            return false;
        }
        return true;
    }

    private Method trouverMethode(String mappingCourant) throws ClassNotFoundException, NoSuchMethodException {
        String[] elements = mappingCourant.split("#", 2);
        if (elements.length != 2) {
            throw new IllegalArgumentException("Mapping invalide : " + mappingCourant);
        }

        String nomClasse = elements[0];
        String nomMethode = elements[1];

        Class<?> classeController = Class.forName(nomClasse);
        return trouverMethode(classeController, nomMethode);
    }

    private Object invoquerMethode(Method methode, HttpServletRequest req, HttpServletResponse res) throws Exception {
        boolean jsonResponse = isJsonResponse(methode);
        String mappingCourant = methode.getDeclaringClass().getName() + "#" + methode.getName();

        if (!jsonResponse
            && !ModelAndView.class.isAssignableFrom(methode.getReturnType())
            && !String.class.isAssignableFrom(methode.getReturnType())) {
            throw new IllegalArgumentException(
                "La methode " + mappingCourant + " doit retourner " + ModelAndView.class.getName()
                    + " ou " + String.class.getName()
            );
        }

        Object instanceController = methode.getDeclaringClass().getDeclaredConstructor().newInstance();
        methode.setAccessible(true);
        Object resultat = methode.invoke(instanceController, construireArguments(methode, req, res));

        if (!jsonResponse && resultat == null) {
            throw new IllegalArgumentException("La methode " + mappingCourant + " a retourne null");
        }

        return resultat;
    }

    private boolean isJsonResponse(Method methode) {
        return methode.isAnnotationPresent(Json.class)
            || methode.isAnnotationPresent(RestApi.class)
            || methode.isAnnotationPresent(ApiRest.class);
    }

    private Object[] construireArguments(Method methode, HttpServletRequest req, HttpServletResponse res) throws Exception {
        Parameter[] parameters = methode.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            if (type.equals(WebApplicationContext.class)) {
                if (springContext == null) {
                    throw new IllegalStateException("Pas de springContext dans le ServletContext");
                }
                arguments[i] = springContext;
            } else if (type.equals(Session.class)) {
                if (sessionManager == null) {
                    throw new IllegalStateException("Pas de SessionManager dans le ServletContext");
                }
                arguments[i] = sessionManager.sessionCourante(req, res);
            } else if (type.equals(SessionManager.class)) {
                if (sessionManager == null) {
                    throw new IllegalStateException("Pas de SessionManager dans le ServletContext");
                }
                arguments[i] = sessionManager;
            } else if (isSimpleType(type) || MapFile.class.isAssignableFrom(type)) {
                arguments[i] = lireValeur(req, parameter.getName(), type);
            } else {
                arguments[i] = construireObjet(type, req);
            }
        }

        return arguments;
    }

    private Object construireObjet(Class<?> type, HttpServletRequest req) throws Exception {
        Constructor<?> constructeur = meilleurConstructeur(type);

        if (constructeur != null && constructeur.getParameterCount() > 0) {
            constructeur.setAccessible(true);
            return constructeur.newInstance(construireArgumentsConstructeur(constructeur, type, req));
        }

        Object instance = type.getDeclaredConstructor().newInstance();
        remplirChamps(instance, req);
        return instance;
    }

    private Constructor<?> meilleurConstructeur(Class<?> type) {
        Constructor<?> meilleur = null;

        for (Constructor<?> constructeur : type.getDeclaredConstructors()) {
            if (constructeur.getParameterCount() == 0) {
                continue;
            }

            if (meilleur == null || constructeur.getParameterCount() > meilleur.getParameterCount()) {
                meilleur = constructeur;
            }
        }

        return meilleur;
    }

    private Object[] construireArgumentsConstructeur(Constructor<?> constructeur, Class<?> type, HttpServletRequest req) throws Exception {
        Parameter[] parameters = constructeur.getParameters();
        Field[] fields = type.getDeclaredFields();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            String nomParametre = parameters[i].isNamePresent() ? parameters[i].getName() : nomChamp(fields, i);
            arguments[i] = lireValeur(req, nomParametre, parameters[i].getType());
        }

        return arguments;
    }

    private String nomChamp(Field[] fields, int index) {
        int position = 0;

        for (Field field : fields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                if (position == index) {
                    return field.getName();
                }
                position++;
            }
        }

        return "arg" + index;
    }

    private void remplirChamps(Object instance, HttpServletRequest req) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (MapFile.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                field.set(instance, lireValeur(req, field.getName(), field.getType()));
                continue;
            }

            String valeur = req.getParameter(field.getName());
            if (valeur == null) {
                continue;
            }

            field.setAccessible(true);
            field.set(instance, convertirParametre(valeur, field.getType()));
        }
    }

    private Object lireValeur(HttpServletRequest req, String name, Class<?> type) throws Exception {
        if (MapFile.class.isAssignableFrom(type)) {
            return FileBinder.read(req, name, type.asSubclass(MapFile.class));
        }
        return convertirParametre(req.getParameter(name), type);
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
            || type.equals(String.class)
            || type.equals(Integer.class)
            || type.equals(Long.class)
            || type.equals(Double.class)
            || type.equals(Float.class)
            || type.equals(Boolean.class)
            || type.equals(Short.class)
            || type.equals(Byte.class)
            || type.equals(Character.class);
    }

    private Object convertirParametre(String valeur, Class<?> type) {
        if (valeur == null || valeur.trim().isEmpty()) {
            return valeurParDefaut(type);
        }

        if (type.equals(String.class)) {
            return valeur;
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.valueOf(valeur);
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.valueOf(valeur);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.valueOf(valeur);
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.valueOf(valeur);
        }
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.valueOf(valeur);
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.valueOf(valeur);
        }
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.valueOf(valeur);
        }
        if (type.equals(char.class) || type.equals(Character.class)) {
            return valeur.charAt(0);
        }

        throw new IllegalArgumentException("Type de parametre non supporte : " + type.getName());
    }

    private Object valeurParDefaut(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type.equals(boolean.class)) {
            return false;
        }
        if (type.equals(char.class)) {
            return '\0';
        }
        if (type.equals(byte.class)) {
            return (byte) 0;
        }
        if (type.equals(short.class)) {
            return (short) 0;
        }
        if (type.equals(int.class)) {
            return 0;
        }
        if (type.equals(long.class)) {
            return 0L;
        }
        if (type.equals(float.class)) {
            return 0F;
        }
        if (type.equals(double.class)) {
            return 0D;
        }

        return null;
    }

    private void writeText(HttpServletResponse res, Object resultat) throws IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(resultat == null ? "" : String.valueOf(resultat));
    }

    private void writeJson(HttpServletResponse res, Object resultat) throws IOException {
        // Un ModelAndView annote expose uniquement ses donnees, sans ouvrir sa vue.
        Object data = resultat instanceof ModelAndView ? ((ModelAndView) resultat).getData() : resultat;
        String json = jsonMapper.writeValueAsString(data);

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private Method trouverMethode(Class<?> classeController, String nomMethode) throws NoSuchMethodException {
        for (Method methode : classeController.getDeclaredMethods()) {
            if (methode.getName().equals(nomMethode)) {
                return methode;
            }
        }

        throw new NoSuchMethodException(classeController.getName() + "#" + nomMethode);
    }

    private void addArgToRequest(HttpServletRequest req, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : safeData(data).entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> safeData(Map<String, Object> data) {
        return data == null ? Collections.emptyMap() : data;
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse res, String view)
        throws ServletException, IOException {
        if (view == null || view.trim().isEmpty()) {
            throw new IllegalArgumentException("La vue du ModelAndView est vide");
        }

        RequestDispatcher dispatcher = req.getRequestDispatcher(view);
        if (dispatcher == null) {
            throw new IllegalArgumentException("Aucun dispatcher trouve pour la vue : " + view);
        }

        dispatcher.forward(req, res);
    }
}
