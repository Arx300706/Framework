package com.framework.upload;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/** Lecture des fichiers pour les arguments, champs et constructeurs du binding. */
public final class FileBinder {
    private FileBinder() {}

    public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
            && "multipart/form-data".equalsIgnoreCase(contentType.split(";", 2)[0].trim());
    }

    public static void prepare(HttpServletRequest request) throws IOException, UploadException {
        if (!isMultipart(request)) {
            return;
        }
        // Parser avant getParameter pour ne pas masquer une requête trop grande.
        try {
            request.getParts();
        } catch (IllegalStateException e) {
            throw new UploadException(413, "La taille maximale de l'upload est depassee.", e);
        } catch (ServletException e) {
            throw new UploadException(400, "Requete multipart invalide.", e);
        }
    }

    public static MapFile read(HttpServletRequest request, String name,
                               Class<? extends MapFile> type) throws Exception {
        if (!isMultipart(request)) {
            return null;
        }
        Part part = request.getPart(name);
        if (part == null || part.getSubmittedFileName() == null) {
            return null;
        }
        // Certains clients transmettent un chemin Windows ou Unix : garder le nom seul.
        String filename = part.getSubmittedFileName().replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        if (filename.isBlank()) {
            return null;
        }
        int dot = filename.lastIndexOf('.');
        String extension = dot > 0 && dot < filename.length() - 1
            ? filename.substring(dot + 1) : "";
        MapFile file = type.getDeclaredConstructor().newInstance();
        file.setNom(filename);
        file.setExtension(extension);
        try (InputStream input = part.getInputStream()) {
            file.setContenu(input.readAllBytes());
        }
        return file; // Un fichier nommé de zéro octet reste valide.
    }

    public static final class UploadException extends Exception {
        private final int status;

        private UploadException(int status, String message, Throwable cause) {
            super(message, cause);
            this.status = status;
        }

        public int getStatus() { return status; }
    }
}
