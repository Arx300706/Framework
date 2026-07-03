package com.framework.core;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Method;
import java.util.List ;
import java.util.ArrayList ;
import java.util.LinkedHashMap;
import java.util.Map;

import com.framework.util.ClasseUtilitaire ;
import com.framework.mapping.UrlMethode;


public class FrontControllerServlet extends HttpServlet {
    ClasseUtilitaire util = new ClasseUtilitaire() ;
    List<String> listNomController = new ArrayList<>() ;
    Map<UrlMethode, String> urlMappings = new LinkedHashMap<>() ;

    public void init() throws ServletException {

        try {
            super.init();
            System.out.println("Démarrage du scan...");
            String annotationValue = "mg.itu.4231.annotation.Controller" ;
            String packageClasse = getServletConfig().getInitParameter("controller-package");
            if (packageClasse == null || packageClasse.trim().isEmpty()) {
                packageClasse = "com.app.controller";
            }

            listNomController = util.findController(annotationValue,packageClasse) ;
            initMapping(annotationValue, packageClasse);

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

    private void initMapping(String annotationValue, String packageClasse) {
        urlMappings = util.findUrlMappings(annotationValue, packageClasse) ;
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException , IOException {
        res.setContentType("text/plain");

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
            Object resultat = invoquerMethode(mappingCourant);

            if (resultat != null) {
                res.getWriter().println(resultat);
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la methode : " + e.getMessage(), e);
        }
    }

    private Object invoquerMethode(String mappingCourant) throws Exception {
        String[] elements = mappingCourant.split("#", 2);
        if (elements.length != 2) {
            throw new IllegalArgumentException("Mapping invalide : " + mappingCourant);
        }

        String nomClasse = elements[0];
        String nomMethode = elements[1];

        Class<?> classeController = Class.forName(nomClasse);
        Object instanceController = classeController.getDeclaredConstructor().newInstance();
        Method methode = classeController.getDeclaredMethod(nomMethode);
        methode.setAccessible(true);

        return methode.invoke(instanceController);
    }
}
