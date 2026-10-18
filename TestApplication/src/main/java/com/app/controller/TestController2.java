package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.Json;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;
import com.app.repository.PersonneRepository;

@Controller("mg.itu.4231.annotation.Controller")
public class TestController2 {
    @urlMapping("/test3")
    @Json
    public ModelAndView tsisy(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Test 3 depuis TestController2");
        return modelAndView;
    }

    @urlMapping("/liste")
    @Json
    public ModelAndView liste() throws Exception {
        PersonneRepository repository = new PersonneRepository();
        repository.initializeDemoData();

        ModelAndView modelAndView = new ModelAndView("/listePersonnes.jsp");
        modelAndView.addAttribut("message", "Liste des personnes depuis la base");
        modelAndView.addAttribut("personnes", repository.findAll());
        modelAndView.addAttribut("total", repository.count());
        return modelAndView;
    }

    @urlMapping("/init-personnes")
    @Json
    public ModelAndView initPersonnes() throws Exception {
        PersonneRepository repository = new PersonneRepository();
        repository.initializeDemoData();

        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Base initialisee avec " + repository.count() + " personnes");
        return modelAndView;
    }
}
