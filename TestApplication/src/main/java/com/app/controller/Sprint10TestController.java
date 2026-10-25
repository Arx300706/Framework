package com.app.controller;

import com.framework.annotation.ApiRest;
import com.framework.annotation.Autorisation;
import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;
import com.framework.session.Session;

@Controller("mg.itu.4231.annotation.Controller")
public class Sprint10TestController {
    @urlMapping(value = "/sprint10-test/login", method = "GET")
    @ApiRest
    public String login(Session session, String role) {
        session.login("utilisateur-test", role);
        return "connecte";
    }

    @urlMapping(value = "/sprint10-test/protege", method = "GET")
    @ApiRest
    @Autorisation
    public String protege() {
        return "autorise";
    }

    @urlMapping(value = "/sprint10-test/admin", method = "GET")
    @ApiRest
    @Autorisation(role = "admin")
    public String admin() {
        return "admin";
    }
}
