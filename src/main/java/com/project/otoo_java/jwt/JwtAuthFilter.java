package com.project.otoo_java.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;
    private final HttpRequestHandlerAdapter httpRequestHandlerAdapter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getHeaderToken(request, "Access");
        String refreshToken = jwtUtil.getHeaderToken(request, "Refresh");
        if (accessToken != null && jwtUtil.validateAccessToken(accessToken) == 1) {
            setAuthentication(jwtUtil.getEmailFromToken(accessToken));
            log.info("Refresh token: {}", refreshToken);
        } else if (refreshToken != null && jwtUtil.validateAccessToken(accessToken) == 2) {
            log.info("Refresh token2: {}", refreshToken);
            TokenDto storedToken = (TokenDto) redisTemplate.opsForValue().get("JWT_TOKEN:" + jwtUtil.getEmailFromToken(refreshToken));
            String storedToken2 = storedToken.getRefreshToken().substring(7);
            if (storedToken2 != null && storedToken2.equals(refreshToken)) {
                String access = jwtUtil.createToken(jwtUtil.getEmailFromToken(refreshToken), "Access");
                request.setAttribute("access", access);
                response.setHeader(JwtUtil.ACCESS_TOKEN, "Bearer " + access);
                jwtUtil.getHeaderToken(request, "Access");
                setAuthentication(jwtUtil.getEmailFromToken(refreshToken));
            } else {
                log.info("Access token expired");
                jwtExceptionHandler(response, "Token expired or user logged out", HttpStatus.UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        Authentication authentication = jwtUtil.createAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void jwtExceptionHandler(HttpServletResponse response, String msg, HttpStatus status)throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;");
        response.setCharacterEncoding("UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(msg);
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
