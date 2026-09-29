package com.mednear.service;

import com.mednear.dto.request.LoginRequest;
import com.mednear.dto.request.RegisterRequest;
import com.mednear.dto.response.AuthResponse;
import com.mednear.entity.Role;
import com.mednear.entity.User;
import com.mednear.exception.BusinessException;
import com.mednear.repository.UserRepository;
import com.mednear.security.JwtUtils;
import com.mednear.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository        userRepository;
    @Autowired private PasswordEncoder       passwordEncoder;
    @Autowired private JwtUtils              jwtUtils;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String          jwt         = jwtUtils.generateJwtToken(auth);
        UserDetailsImpl principal   = (UserDetailsImpl) auth.getPrincipal();
        String          role        = principal.getAuthorities().iterator()
                                          .next().getAuthority()
                                          .replace("ROLE_", "");

        return new AuthResponse(jwt, principal.getId(),
                                principal.getName(), principal.getEmail(), role);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email is already registered: " + req.getEmail());
        }

        Role role = (req.getRole() != null && req.getRole().equalsIgnoreCase("OWNER"))
                    ? Role.OWNER : Role.CUSTOMER;

        User user = new User(req.getName(), req.getEmail(),
                             passwordEncoder.encode(req.getPassword()), role);
        user = userRepository.save(user);

        // Auto-login: return a token immediately after registration
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        String jwt = jwtUtils.generateJwtToken(auth);

        return new AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), role.name());
    }
}
