package com.mednear.controller;

import com.mednear.dto.request.LoginRequest;
import com.mednear.dto.request.RegisterRequest;
import com.mednear.dto.response.ApiResponse;
import com.mednear.dto.response.AuthResponse;
import com.mednear.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    /** POST /api/auth/login */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        AuthResponse data = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
    }

    /** POST /api/auth/register */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        AuthResponse data = authService.register(req);
        return ResponseEntity.status(201).body(ApiResponse.ok("Registration successful", data));
    }
}
