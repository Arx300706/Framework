package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Protège une action (ou toutes les actions d'un contrôleur).
 * Sans rôle, une authentification suffit. Avec un rôle, celui-ci est obligatoire.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Autorisation {
    String role() default "";
}
