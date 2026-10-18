package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.ApiRest;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;
import com.app.model.User;

@Controller("mg.itu.4231.annotation.Controller")
public class TestController3 {
    @urlMapping("/test4")
    public ModelAndView test3(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Test 4 depuis TestController3");
        return modelAndView;
    }

    @urlMapping("/details")
    public ModelAndView details(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Details");
        modelAndView.addAttribut("key1", "valeur 1");
        modelAndView.addAttribut("key2", "valeur 2");
        return modelAndView;
    }

    @urlMapping(value = "/login", method = "POST")
    public String loginPost(Integer numero, String nom) {
        System.out.println("Login method POST appelee ! " + numero + " et le nom est : " + nom);
        return "LoginPost method POST appele ! le numero est : " + numero + " et le nom est : " + nom;
    }

    @urlMapping(value = "/param", method = "POST")
    @ApiRest
    public User testParametresObject(User user) {
        System.out.println(
            "Le numero est : " + user.getId()
            + " et le nom est : " + user.getName()
            + " et l'email est : " + user.getEmail()
        );

        return new User(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }
}
