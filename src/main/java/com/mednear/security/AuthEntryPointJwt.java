package com.mednear.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mednear.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a proper JSON 401 body instead of Spring's default HTML error page.
 * Called whenever an unauthenticated request hits a protected endpoint.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest  request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(401, "Unauthorized",
            "Authentication required. Please provide a valid Bearer token.");
        mapper.writeValue(response.getOutputStream(), body);
    }
}
