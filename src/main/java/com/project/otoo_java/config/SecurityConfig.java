package com.project.otoo_java.config;

import com.project.otoo_java.jwt.JwtAuthFilter;
import com.project.otoo_java.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil; // JWT 관련 유틸리티 클래스 주입
    private final RedisTemplate<String, Object> redisTemplate; // Redis 템플릿 주입
    private final HttpRequestHandlerAdapter httpRequestHandlerAdapter; // HTTP 요청 핸들러 어댑터 주입

    @Value("${FASTAPI_URL}") // FASTAPI 서버 URL
    private String FASTAPI_URL;

    @Value("${REACT_URL}") // REACT 앱 URL
    private String REACT_URL;

    @Value("${REST_URL}") // REST API 서버 URL
    private String REST_URL;

    // 허용할 URL 패턴 배열
    private static final String[] PERMIT_URL_ARRAY = {
            /* swagger v3 */
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",

            // 인증 없이 접근 가능한 URL들
            //챗봇
            "/chatbot",
            "/emotionReport",
            "/qna",
            //피드백
            "/feedback",
            //로그인
            "/login",
            "/kakaoLogin/**",
            "/naverLogin",
            "/naverLogin/callbacks",
            "/googleLogin/callbacks",
            "/newtoken/**",
            "/logoutUser",
            //이메일인증
            "/email/**",
            "/forgotpassword/**",
            //ocr
            "/api/conflict/ocr",
            "/api/love/ocr",
            "/api/friendship/ocr",
            //회원가입
            "/users",
            "/changePwd",
            "/join",
            "/users",
            "/changePwd",
            //분석
            "/api/conflict/analysis",
            "/api/love/analysis",
            "/api/friendship/analysis",
            //stt
            "/api/transcribe/file",
            "/api/transcribe/websocket",
            "/transcribe",
            "/audio-stream",
            "/api/transcribe/websocket",
            //board
            "/api/posts/**",
            //관리자
            "/getAllUser",
            "/admin/changeStatusBan",
            "/admin/changeStatusNotBan",
            "/admin/getGenderOne/{usersGender}",
            "/admin/getBanOne/{usersBan}"
    };

    // BCryptPasswordEncoder 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정 빈 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 허용할 오리진 패턴 설정
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfiguration.addAllowedOrigin(REACT_URL);
        corsConfiguration.addAllowedOriginPattern("*"); // 모든 오리진 패턴 허용
        corsConfiguration.setAllowedMethods(Arrays.asList("POST", "GET", "DELETE", "PUT", "PATCH")); // 허용할 HTTP 메서드 설정
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        corsConfiguration.setAllowCredentials(true); // 인증 정보를 서버에 전달할 수 있도록 설정
        corsConfiguration.addExposedHeader("*"); // 모든 헤더 노출

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // 모든 경로에 대해 CORS 설정 적용
        return source;
    }

    // SecurityFilterChain 설정 빈 등록
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil, redisTemplate, httpRequestHandlerAdapter);

        // CSRF 보안 설정 비활성화
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults()); // CORS 설정 적용

        http
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않음
                );

        http
                //.csrf(csrf -> csrf.disable()) // CSRF 보안 설정 다시 비활성화
                .authorizeHttpRequests((auth) ->
                        auth
                                // 인증없이 허용 URL
                                .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                                // 특정 URL 허용
                                .requestMatchers("/user/**").hasRole("USER")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated() // 나머지 요청에 대해 인증 필요
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가

        return http.build(); // SecurityFilterChain 반환
    }

}
