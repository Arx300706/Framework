package com.framework.core;

import java.util.List;
import java.util.Map;

import com.framework.mapping.UrlMethode;
import com.framework.util.ClasseUtilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class FrontControllerListner implements ServletContextListener {
    public static final String CONTROLLERS_ATTRIBUTE = "framework.controllers";
    public static final String URL_MAPPINGS_ATTRIBUTE = "framework.urlMappings";
    public static final String SPRING_ROOT = "org.springframework.web.context.WebApplicationContext.ROOT";

    private static final String DEFAULT_ANNOTATION_VALUE = "mg.itu.4231.annotation.Controller";
    private static final String DEFAULT_CONTROLLER_PACKAGE = "com.app.controller";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ClasseUtilitaire util = new ClasseUtilitaire();

        String annotationValue = DEFAULT_ANNOTATION_VALUE;
        String packageClasse = context.getInitParameter("controller-package");
        if (packageClasse == null || packageClasse.trim().isEmpty()) {
            packageClasse = DEFAULT_CONTROLLER_PACKAGE;
        }

        List<String> controllers = util.findController(annotationValue, packageClasse);
        Map<UrlMethode, String> urlMappings = util.findUrlMappings(annotationValue, packageClasse);

        context.setAttribute(CONTROLLERS_ATTRIBUTE, controllers);
        context.setAttribute(URL_MAPPINGS_ATTRIBUTE, urlMappings);
        context.setAttribute("springContext", context.getAttribute(SPRING_ROOT));
    }
}
