package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.RestApi;
import com.framework.annotation.urlMapping;
import com.framework.core.ModelAndView;

@Controller("mg.itu.4231.annotation.Controller")
public class TestController3 {
    @urlMapping("/test4")
    @RestApi
    public ModelAndView test3(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Test 4 depuis TestController3");
        return modelAndView;
    }

    @urlMapping("/details")
    @RestApi
    public ModelAndView details(){
        ModelAndView modelAndView = new ModelAndView("/view.jsp");
        modelAndView.addAttribut("message", "Details");
        return modelAndView;
    }
}
