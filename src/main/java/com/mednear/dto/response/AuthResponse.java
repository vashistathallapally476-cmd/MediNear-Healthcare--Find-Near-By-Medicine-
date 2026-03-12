package com.mednear.dto.response;

/**
 * Returned on successful login.
 * Flat structure — frontend reads token, id, name, role directly.
 */
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long   id;
    private String name;
    private String email;
    private String role;

    public AuthResponse(String token, Long id, String name, String email, String role) {
        this.token = token;
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.role  = role;
    }

    public String getToken()     { return token; }
    public String getTokenType() { return tokenType; }
    public Long   getId()        { return id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getRole()      { return role; }
}
