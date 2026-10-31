package com.strangerchat.dto;

public class GuestAuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String guestId;
    private String displayName;
    private boolean isGuest;

    public GuestAuthResponse() {}

    public GuestAuthResponse(String token, String tokenType, String guestId, String displayName, boolean isGuest) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.guestId = guestId;
        this.displayName = displayName;
        this.isGuest = isGuest;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isGuest() { return isGuest; }
    public void setGuest(boolean guest) { isGuest = guest; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String tokenType = "Bearer";
        private String guestId;
        private String displayName;
        private boolean isGuest = true;

        public Builder token(String token) { this.token = token; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder guestId(String guestId) { this.guestId = guestId; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder isGuest(boolean isGuest) { this.isGuest = isGuest; return this; }

        public GuestAuthResponse build() {
            return new GuestAuthResponse(token, tokenType, guestId, displayName, isGuest);
        }
    }
}
