package com.framework.util;
import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;
import com.framework.mapping.UrlMethode;

import java.util.List ;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Method;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.util.Set;

public class ClasseUtilitaire {
    public ClasseUtilitaire(){} ;

    public List<String> findController(String annotationValue,String packageClasse){
        List<String> liste = new ArrayList<>() ;

        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));

        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if( clazz.isAnnotationPresent(Controller.class)){
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                String valeur = controllerAnnotation.value() ;

                if (valeur.equals(annotationValue)){
                    liste.add(clazz.getName()) ;
                }
            } 
        }
        return liste ;
    }

    public Map<UrlMethode, String> findUrlMappings(String annotationValue, String packageClasse) {
        Map<UrlMethode, String> mappings = new LinkedHashMap<>();

        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));
        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                String valeur = controllerAnnotation.value();

                if (valeur.equals(annotationValue)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(urlMapping.class)) {
                            urlMapping mapping = method.getAnnotation(urlMapping.class);
                            UrlMethode urlMethode = new UrlMethode(mapping.value(), mapping.method());
                            String methodeMapping = clazz.getName() + "#" + method.getName();

                            if (mappings.containsKey(urlMethode)) {
                                throw new RuntimeException(
                                    "Route dupliquee : " + urlMethode + " existe deja pour " + mappings.get(urlMethode)
                                        + ", impossible d'ajouter " + methodeMapping
                                );
                            }

                            mappings.put(urlMethode, methodeMapping);
                        }
                    }
                }
            }
        }

        return mappings;
    }
}
