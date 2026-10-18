package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.Json;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;

@Controller("mg.itu.4231.annotation.Controller")
public class TestController1 {
    @urlMapping("/test1")
    @Json
    public ModelAndView test1(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "GET test1");
        return modelAndView;
    }

    @urlMapping(value = "/test1", method = "POST")
    @Json
    public ModelAndView test1Post(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "POST test1");
        return modelAndView;
    }

    @urlMapping("/accueil")
    @Json
    public ModelAndView accueil(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Bienvenue");
        return modelAndView;
    }
}
