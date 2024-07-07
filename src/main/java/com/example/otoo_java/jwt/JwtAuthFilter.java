package com.example.otoo_java.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getHeaderToken(request, "Access");
        String refreshToken = jwtUtil.getHeaderToken(request, "Refresh");

        if (accessToken != null) {

            if (jwtUtil.validateAccessToken(accessToken) == 1) {
                setAuthentication(jwtUtil.getEmailFromToken(accessToken));
            } else if (jwtUtil.validateAccessToken(accessToken) == 2) {
                jwtExceptionHandler(response, "REISSUE", HttpStatus.SEE_OTHER);
                return;
            }
        } else if (refreshToken != null) {
            if (jwtUtil.refreshTokenValidation(refreshToken)) {
                setAuthentication(jwtUtil.getEmailFromToken(refreshToken));
            }
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        Authentication authentication = jwtUtil.createAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void jwtExceptionHandler(HttpServletResponse response, String msg, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(status.toString());
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}