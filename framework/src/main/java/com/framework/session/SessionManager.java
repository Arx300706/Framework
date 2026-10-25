package com.framework.session;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Une instance de Session par client, stockée dans une map du contexte applicatif. */
public final class SessionManager {
    public static final String ATTRIBUTE = "framework.sessions";
    public static final String COOKIE = "FRAMEWORK_SESSION";
    public static final int TIMEOUT_MINUTES_PAR_DEFAUT = 30;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long timeoutMillis;

    public SessionManager(int timeoutMinutes) {
        this.timeoutMillis = timeoutMinutes * 60_000L;
    }

    public Set<String> getSessionIds() {
        return Set.copyOf(sessions.keySet());
    }

    public Session sessionCourante(HttpServletRequest req, HttpServletResponse res) {
        String id = lireCookie(req);
        Session session = id == null ? null : sessions.get(id);

        if (session != null && session.estActive(timeoutMillis)) {
            return session;
        }
        if (session != null) {
            sessions.remove(id, session);
        }

        // Nettoyage ici seulement : c'est la création qui fait grossir la map.
        sessions.values().removeIf(existante -> !existante.estActive(timeoutMillis));

        Session nouvelle = new Session(UUID.randomUUID().toString());
        sessions.put(nouvelle.getId(), nouvelle);
        res.addCookie(creerCookie(nouvelle.getId()));
        return nouvelle;
    }

    private String lireCookie(HttpServletRequest req) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : req.getCookies()) {
            if (COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Cookie creerCookie(String id) {
        Cookie cookie = new Cookie(COOKIE, id);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
