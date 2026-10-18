package com.framework.mapping;

import java.util.Objects;

public class UrlMethode {
    private String url;
    private String methode;

    public UrlMethode(String url, String methode) {
        this.url = url;
        this.methode = methode == null ? null : methode.toUpperCase();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode == null ? null : methode.toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof UrlMethode)) {
            return false;
        }

        UrlMethode other = (UrlMethode) obj;
        return Objects.equals(url, other.url) && Objects.equals(methode, other.methode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methode);
    }

    @Override
    public String toString() {
        return methode + " " + url;
    }
}
