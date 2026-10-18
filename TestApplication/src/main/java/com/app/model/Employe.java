package com.app.model;

import com.framework.upload.MapFile;

public class Employe {
    private String nom;
    private MapFile photo;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public MapFile getPhoto() { return photo; }
    public void setPhoto(MapFile photo) { this.photo = photo; }
}
