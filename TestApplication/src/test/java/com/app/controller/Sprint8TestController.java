package com.app.controller;

import com.framework.annotation.ApiRest;
import com.framework.annotation.Controller;
import com.framework.annotation.urlMapping;
import com.framework.upload.MapFile;

@Controller("mg.itu.4231.annotation.Controller")
public class Sprint8TestController {
    @urlMapping(value = "/sprint8-test/direct", method = "POST")
    @ApiRest
    public MapFile direct(MapFile photo) { return photo; }

    @urlMapping(value = "/sprint8-test/legacy", method = "POST")
    @ApiRest
    public com.app.model.MapFile legacy(com.app.model.MapFile photo) { return photo; }

    @urlMapping(value = "/sprint8-test/constructor", method = "POST")
    @ApiRest
    public PhotoRecord constructor(PhotoRecord employe) { return employe; }

    public static class PhotoRecord {
        private final String nom;
        private final Integer numero;
        private final MapFile photo;

        public PhotoRecord(String nom, Integer numero, MapFile photo) {
            this.nom = nom;
            this.numero = numero;
            this.photo = photo;
        }

        public String getNom() { return nom; }
        public Integer getNumero() { return numero; }
        public MapFile getPhoto() { return photo; }
    }
}
