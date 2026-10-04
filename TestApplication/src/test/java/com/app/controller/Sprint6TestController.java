package com.app.controller;

import com.app.model.Personne;
import com.framework.annotation.Controller;
import com.framework.annotation.RestApi;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.context.WebApplicationContext;

@Controller("mg.itu.4231.annotation.Controller")
public class Sprint6TestController {
    @urlMapping("/sprint6-test/object")
    @RestApi
    public Personne personne() {
        return new Personne(7, "Aïna \"test\"\nAntananarivo", 21);
    }

    @urlMapping("/sprint6-test/list")
    @RestApi
    public List<Personne> personnes() {
        return Arrays.asList(new Personne(1, "Aina", 21), new Personne(2, "Miora", 24));
    }

    @urlMapping("/sprint6-test/string")
    @RestApi
    public String texte() {
        return "Bonjour \"été\"\nC:\\test\t!";
    }

    @urlMapping("/sprint6-test/number")
    @RestApi
    public int nombre() {
        return 42;
    }

    @urlMapping("/sprint6-test/boolean")
    @RestApi
    public boolean booleen() {
        return true;
    }

    @urlMapping("/sprint6-test/array")
    @RestApi
    public int[] tableau() {
        return new int[] {1, 2, 3};
    }

    @urlMapping("/sprint6-test/null")
    @RestApi
    public Object valeurNulle() {
        return null;
    }

    @urlMapping("/sprint6-test/data")
    @RestApi
    public ModelAndView donnees() {
        ModelAndView model = new ModelAndView(null);
        model.addAttribut("personne", personne());
        model.addAttribut("absent", null);
        return model;
    }

    @urlMapping("/sprint6-test/spring")
    @RestApi
    public boolean contexteSpring(WebApplicationContext context) {
        return context != null;
    }

    @urlMapping("/sprint6-test/view")
    public ModelAndView vue() {
        ModelAndView model = new ModelAndView("/view.jsp");
        model.addAttribut("message", "Vue du sprint 5");
        return model;
    }

    @urlMapping("/sprint6-test/invalid")
    public String retourInvalide() {
        return "Ce retour necessite @RestApi";
    }

    @urlMapping("/sprint6-test/null-view")
    public ModelAndView vueNulle() {
        return null;
    }
}
