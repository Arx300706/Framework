package com.framework.core;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List ;
import java.util.ArrayList ;
import java.util.LinkedHashMap;
import java.util.Map;

import com.framework.mapping.UrlMethode;
import com.framework.util.Util;

import org.springframework.web.context.WebApplicationContext;


public class FrontControllerServlet extends HttpServlet {
    List<String> listNomController = new ArrayList<>() ;
    Map<UrlMethode, String> urlMappings = new LinkedHashMap<>() ;
    WebApplicationContext springContext;

    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        try {
            super.init();

            ServletContext context = getServletContext();
            Object controllersAttribute = context.getAttribute(FrontControllerListner.CONTROLLERS_ATTRIBUTE);
            Object mappingsAttribute = context.getAttribute(FrontControllerListner.URL_MAPPINGS_ATTRIBUTE);
            Object springContextAttribute = context.getAttribute("springContext");

            if (controllersAttribute instanceof List<?>) {
                listNomController = (List<String>) controllersAttribute;
            }

            if (mappingsAttribute instanceof Map<?, ?>) {
                urlMappings = (Map<UrlMethode, String>) mappingsAttribute;
            }

            if (springContextAttribute instanceof WebApplicationContext) {
                springContext = (WebApplicationContext) springContextAttribute;
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
            ModelAndView modelAndView = invoquerMethode(mappingCourant);
            addArgToRequest(req, modelAndView.getData());
            dispatch(req, res, modelAndView.getView());

        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la methode : " + e.getMessage(), e);
        }
    }

    private ModelAndView invoquerMethode(String mappingCourant) throws Exception {
        String[] elements = mappingCourant.split("#", 2);
        if (elements.length != 2) {
            throw new IllegalArgumentException("Mapping invalide : " + mappingCourant);
        }

        String nomClasse = elements[0];
        String nomMethode = elements[1];

        Class<?> classeController = Class.forName(nomClasse);
        Object instanceController = classeController.getDeclaredConstructor().newInstance();
        Method methode = trouverMethode(classeController, nomMethode);

        if (!ModelAndView.class.isAssignableFrom(methode.getReturnType())) {
            throw new IllegalArgumentException(
                "La methode " + mappingCourant + " doit retourner " + ModelAndView.class.getName()
            );
        }

        methode.setAccessible(true);

        Object resultat;
        if (Util.haveParameter(methode, WebApplicationContext.class)) {
            if (springContext == null) {
                throw new IllegalStateException("Pas de springContext dans le ServletContext");
            }

            resultat = methode.invoke(instanceController, springContext);
        } else {
            resultat = methode.invoke(instanceController);
        }

        if (resultat == null) {
            throw new IllegalArgumentException("La methode " + mappingCourant + " a retourne null");
        }

        return (ModelAndView) resultat;
    }

    private Method trouverMethode(Class<?> classeController, String nomMethode) throws NoSuchMethodException {
        for (Method methode : classeController.getDeclaredMethods()) {
            if (methode.getName().equals(nomMethode)
                && (methode.getParameterCount() == 0 || Util.haveParameter(methode, WebApplicationContext.class))) {
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
