# 📖 갈등 판결 서비스 '몇대몇' (RestAPI)

이 프로젝트는 다양한 RESTful API를 제공하는 서버 애플리케이션입니다. <br/> 
주요 기능으로는 STT API, Redis, JWT 인증, JPA(ORM)를 사용한 데이터 관리 등이 포함됩니다.

# 구 조
![Group 2245](https://github.com/user-attachments/assets/84960185-daeb-49fc-bf14-2e21980a05b0)


# 프로젝트 설치 및 실행

- 전제 조건
    - Java 17 이상
    - Spring Boot 3.0


환경변수 설정 (application.yml)
```

  jwt:
    secretKey: ${JWT_SECRET_KEY}

    access:
      expiration: 3600000
      header: Authorization

    refresh:
      expiration: 1209600000
      header: Authorization-refresh

  oauth:
    kakao:
      client_id: ${OAUTH_KAKAO_CLIENT_ID}
      url:
        auth: https://kauth.kakao.com
        api: https://kapi.kakao.com
    naver:
      client_id: ${OAUTH_NAVER_CLIENT_ID}
      client_secret: ${OAUTH_NAVER_CLIENT_SECRET}
      redirect_uri: 
    google:
      client_id: ${OAUTH_GOOGLE_CLIENT_ID}
      client_secret: ${OAUTH_GOOGLE_CLIENT_SECRET}
      redirect_uri: 


  spring:
    application:
      name: otoo_java
    datasource:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: ${SPRING_DATASOURCE_URL}
      username: ${SPRING_DATASOURCE_USERNAME}
      password: ${SPRING_DATASOURCE_PASSWORD}
    jpa:
      show-sql: true
      hibernate:
        ddl-auto: update
        properties:
          hibernate:
            format_sql: true
            dialect: org.hibernate.dialect.MySQLDialect
    mail:
      host: ${SPRING_MAIL_HOST}
      port: ${SPRING_MAIL_PORT}
      username: ${SPRING_MAIL_USERNAME}
      password: ${SPRING_MAIL_PASSWORD}
      properties:
        mail:
          smtp:
            auth: true
            starttls:
              enable: true
              required: true
            ssl:
              trust: ${SPRING_MAIL_HOST}


  fastapi:
    url: ${FASTAPI_URL}

  react:
    url: ${REACT_URL}

  rest:
    url: ${REST_URL}

  data:
    redis:
      port: 6379
      host: localhost

  server:
    port: 8080



  management:
    endpoints:
      web:
        exposure:
          include: health,info
    endpoint:
      health:
        show-details: always
```

Redis 설치 및 실행

```
https://github.com/microsoftarchive/redis/releases 설치 -> redis-server 실행
```

# 주요기능 및 예제코드

## JWT 인증

### 설명

- JWT(JSON Web Token)를 사용하여 인증 및 권한 부여를 구현합니다. 이 기능은 사용자의 로그인 상태를 유지하고, <br/> 권한 있는 사용자만이 특정 API에 접근할 수 있도록 합니다.

### 예제코드
```
// JwtUtil.java

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final PrincipalDetailsService principalDetailsService;
    private static final long ACCESS_TIME = 24 * 60 * 60 * 1000L; // 1일
    private static final long REFRESH_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(String email, String type) {
        Date date = new Date();
        long time = type.equals("Access") ? ACCESS_TIME : REFRESH_TIME;
        Claims claims = Jwts.claims().setSubject(email).setIssuedAt(date).setExpiration(new Date(date.getTime() + time));
        return Jwts.builder().setClaims(claims).signWith(key, signatureAlgorithm).compact();
    }

    public boolean tokenValidation(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}

```

## Redis 캐싱

### 설명

- Redis를 사용하여 캐싱 기능을 구현하였습니다. <br/> 이를 통해 데이터 접근 속도를 향상시킬 수 있습니다.
  
### 예제코드
```
// RedisRefreshToken.java

@NoArgsConstructor
@Getter
@RedisHash("refreshToken")
public class RedisRefreshToken {
    @Id
    private String id;
    private String refreshToken;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiration;

    public RedisRefreshToken(String email, String refreshToken, Long expiration) {
        this.id = email;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}

```

## STT API

### 설명

- 음성 텍스트 변환(STT) API를 제공하여 음성 데이터를 텍스트로 변환합니다.<br/>  VITO API를 사용하여 음성 데이터를 처리합니다.
  
### 예제코드
```
// SttService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class SttService {
    @Value("${vito.client_id}")
    String client_id;

    @Value("${vito.client_secret}")
    String client_secret;

    @Value("${FASTAPI_URL}")
    String fastApiUrl;

    public String getAccessToken() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai")
                .build();

        return webClient.post()
                .uri("/v1/authenticate")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public ResponseEntity<String> transcribeFile(MultipartFile multipartFile, String usercode) {
        String accessToken = getAccessToken();
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai/v1")
                .build();

        String response = webClient.post()
                .uri("/transcribe")
                .bodyValue(multipartFile.getBytes())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("STT Response: {}", response);
        return ResponseEntity.ok(response);
    }
}

```

## 보안 설정

### 설명

- Spring Security와 JWT를 사용하여 보안 기능을 설정하였습니다. <br/> 이를 통해 API 요청에 대한 인증 및 권한 부여를 구현합니다.

### 예제코드
```
// JwtAuthFilter.java

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getHeaderToken(request, "Access");
        if (accessToken != null && jwtUtil.tokenValidation(accessToken)) {
            Authentication authentication = jwtUtil.createAuthentication(jwtUtil.getEmailFromToken(accessToken));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}

```


## JPA (ORM)

### 설명

- JPA를 사용하여 데이터베이스와 상호작용하는 ORM(Object-Relational Mapping)을 구현하였습니다.<br/>  이를 통해 데이터베이스의 데이터를 객체로 다루고, 다양한 쿼리를 쉽게 작성할 수 있습니다.

### 예제코드
```
// Repository.java

@Repository
public interface SttTalksRepository extends JpaRepository<SttTalks, Long> {
}

```
