package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;

@Controller("mg.itu.4231.annotation.Controller")
public class TestController1 {
    @urlMapping("/test1")
    public void test1(){

    }

    @urlMapping(value = "/test1", method = "POST")
    public String test1Post(){
        return "POST test1";
    }

    @urlMapping("/accueil")
    public void accueil(){

    }
}
