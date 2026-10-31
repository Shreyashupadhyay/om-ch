package com.strangerchat.security;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final String sessionId;
    private final UserPrincipal userPrincipal;

    public StompPrincipal(String sessionId, UserPrincipal userPrincipal) {
        this.sessionId = sessionId;
        this.userPrincipal = userPrincipal;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserPrincipal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public String getName() {
        return sessionId;
    }
}
