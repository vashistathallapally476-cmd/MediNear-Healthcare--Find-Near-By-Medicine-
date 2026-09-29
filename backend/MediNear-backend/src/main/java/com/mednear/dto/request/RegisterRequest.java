package com.mednear.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    // Optional – defaults to CUSTOMER if null/missing
    @Pattern(regexp = "^(CUSTOMER|OWNER)$", message = "Role must be CUSTOMER or OWNER")
    private String role;

    public String getName()             { return name; }
    public void   setName(String v)     { this.name = v; }
    public String getEmail()            { return email; }
    public void   setEmail(String v)    { this.email = v; }
    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }
    public String getRole()             { return role; }
    public void   setRole(String v)     { this.role = v; }
}
