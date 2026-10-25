package com.app.controller;

import com.framework.annotation.ApiRest;
import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;
import com.framework.session.Session;
import com.framework.session.SessionManager;
import java.util.Set;

@Controller("mg.itu.4231.annotation.Controller")
public class Sprint9TestController {
    @urlMapping(value = "/sessions", method = "GET")
    @ApiRest
    public Set<String> sessions(SessionManager sessionManager) {
        return sessionManager.getSessionIds();
    }

    @urlMapping(value = "/sprint9-test/set", method = "GET")
    @ApiRest
    public Object set(Session session, String valeur) {
        session.set("valeur", valeur);
        return session.get("valeur");
    }

    @urlMapping(value = "/sprint9-test/get", method = "GET")
    @ApiRest
    public Object get(Session session) {
        return session.get("valeur");
    }

    @urlMapping(value = "/sprint9-test/remove", method = "GET")
    @ApiRest
    public Object remove(Session session) {
        session.remove("valeur");
        return session.get("valeur");
    }

    @urlMapping(value = "/sprint9-test/invalidate", method = "GET")
    @ApiRest
    public Object invalidate(Session session) {
        session.invalidate();
        return "invalidee";
    }
}
