package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.Json;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;
import org.springframework.web.context.WebApplicationContext;

/** Cas internes aux tests automatises, sans routes de demonstration. */
@Controller("mg.itu.4231.annotation.Controller")
public class Sprint6TestController {
    @urlMapping("/sprint6-test/spring")
    @Json
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
    public Integer retourInvalide() {
        return 7;
    }

    @urlMapping("/sprint6-test/null-view")
    public ModelAndView vueNulle() {
        return null;
    }

    
    public String test(){
        return "ok";
    }
}
