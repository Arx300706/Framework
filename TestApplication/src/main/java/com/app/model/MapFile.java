package com.app.model;

/** Compatibilité avec l'ancien import ; le type réutilisable appartient au framework. */
public class MapFile extends com.framework.upload.MapFile {
    public MapFile() {}

    public MapFile(String nom, String extension, byte[] contenu) {
        super(nom, extension, contenu);
    }
}
