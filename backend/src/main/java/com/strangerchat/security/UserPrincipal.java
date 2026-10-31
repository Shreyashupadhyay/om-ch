package com.strangerchat.security;

import com.strangerchat.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class UserPrincipal implements UserDetails, Principal {

    private final UUID id;
    private final String guestId;
    private final String username;
    private final String email;
    private final String password;
    private final boolean isGuest;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String guestId, String username, String email, String password, boolean isGuest, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.guestId = guestId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isGuest = isGuest;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .guestId(null)
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .isGuest(false)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }

    public static UserPrincipal createGuest(String guestId, String displayName) {
        return UserPrincipal.builder()
                .id(null)
                .guestId(guestId)
                .username(displayName)
                .email(null)
                .password(null)
                .isGuest(true)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST")))
                .build();
    }

    public UUID getId() { return id; }
    public String getGuestId() { return guestId; }
    public String getEmail() { return email; }
    public boolean isGuest() { return isGuest; }

    public String getPrincipalKey() {
        return isGuest ? ("guest:" + guestId) : ("user:" + id.toString());
    }

    @Override
    public String getName() {
        return getPrincipalKey();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String guestId;
        private String username;
        private String email;
        private String password;
        private boolean isGuest;
        private Collection<? extends GrantedAuthority> authorities;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder guestId(String guestId) { this.guestId = guestId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder isGuest(boolean isGuest) { this.isGuest = isGuest; return this; }
        public Builder authorities(Collection<? extends GrantedAuthority> authorities) { this.authorities = authorities; return this; }

        public UserPrincipal build() {
            return new UserPrincipal(id, guestId, username, email, password, isGuest, authorities);
        }
    }
}
