package com.framework.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Session propre au framework : injectée dans les méthodes d'action, sans HttpSession. */
public final class Session {
    public static final String AUTHENTICATED_KEY = "authenticated";
    public static final String USER_KEY = "user";
    public static final String ROLE_KEY = "role";
    private final String id;
    private final Map<String, Object> attributs = new ConcurrentHashMap<>();
    private volatile long dernierAcces;
    private volatile boolean active = true;

    Session(String id) {
        this.id = id;
        this.dernierAcces = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public Object get(String cle) {
        toucher();
        return attributs.get(cle);
    }

    public void set(String cle, Object valeur) {
        toucher();
        // ConcurrentHashMap refuse les valeurs null : une valeur null équivaut à une suppression.
        if (valeur == null) {
            attributs.remove(cle);
        } else {
            attributs.put(cle, valeur);
        }
    }

    public void remove(String cle) {
        toucher();
        attributs.remove(cle);
    }

    /** Convention d'authentification proposée aux applications utilisant le framework. */
    public void login(Object utilisateur, String role) {
        set(USER_KEY, utilisateur);
        set(ROLE_KEY, role);
        set(AUTHENTICATED_KEY, Boolean.TRUE);
    }

    public void logout() {
        remove(USER_KEY);
        remove(ROLE_KEY);
        remove(AUTHENTICATED_KEY);
    }

    public boolean estAuthentifie() {
        return Boolean.TRUE.equals(get(AUTHENTICATED_KEY)) || get(USER_KEY) != null;
    }

    public boolean aLeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return estAuthentifie();
        }
        Object roleCourant = get(ROLE_KEY);
        return estAuthentifie() && roleCourant != null
            && role.trim().equalsIgnoreCase(String.valueOf(roleCourant).trim());
    }

    public void invalidate() {
        active = false;
        attributs.clear();
    }

    boolean estActive(long timeoutMillis) {
        return active && System.currentTimeMillis() - dernierAcces <= timeoutMillis;
    }

    private void toucher() {
        dernierAcces = System.currentTimeMillis();
    }
}
