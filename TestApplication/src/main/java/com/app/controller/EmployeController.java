package com.app.controller;

import com.app.model.Employe;
import com.framework.annotation.ApiRest;
import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;

@Controller("mg.itu.4231.annotation.Controller")
public class EmployeController {
    @urlMapping(value = "/employe/save", method = "POST")
    @ApiRest
    public Employe save(Employe employe) {
        // Le framework a déjà rempli nom et photo.
        return employe;
    }
}
