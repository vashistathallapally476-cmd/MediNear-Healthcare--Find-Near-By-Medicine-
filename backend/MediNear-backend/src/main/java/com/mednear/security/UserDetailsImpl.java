package com.mednear.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mednear.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {

    private final Long   id;
    private final String name;
    private final String email;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String name, String email,
                           String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.name        = name;
        this.email       = email;
        this.password    = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public Long   getId()    { return id; }
    public String getEmail() { return email; }
    public String getName()  { return name; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String  getPassword()               { return password; }
    @Override public String  getUsername()               { return email; }  // email IS the username
    @Override public boolean isAccountNonExpired()       { return true; }
    @Override public boolean isAccountNonLocked()        { return true; }
    @Override public boolean isCredentialsNonExpired()   { return true; }
    @Override public boolean isEnabled()                 { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl u)) return false;
        return Objects.equals(id, u.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
