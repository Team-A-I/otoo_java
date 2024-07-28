package com.project.otoo_java.login.model;

import com.project.otoo_java.auth.PrincipalDetails;
import com.project.otoo_java.enums.OAuthProvider;
import com.project.otoo_java.jwt.JwtUtil;
import com.project.otoo_java.jwt.TokenDto;
import com.project.otoo_java.login.dto.GoogleOAuthResponseDto;
import com.project.otoo_java.login.dto.LoginRequestDto;
import com.project.otoo_java.login.dto.UserResponseDto;
import com.project.otoo_java.users.model.dto.UsersDto;
import com.project.otoo_java.users.model.entity.Users;
import com.project.otoo_java.users.model.repository.UsersRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final UsersRepository usersRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;

    @Value("${oauth.naver.client_id}")
    private String naverClientId;

    @Value("${oauth.naver.client_secret}")
    private String naverClientSecret;

    @Value("${oauth.naver.redirect_uri}")
    private String naverRedirectUri;

    @Value("${oauth.google.client_id}")
    private String googleClientId;

    @Value("${oauth.google.client_secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect_uri}")
    private String googleRedirectUri;

    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());
    }

    // 비밀번호 변경시 기존 비밀번호 확인
    public void checkFormerPwd(UsersDto usersDto) throws Exception {
        //비밀번호 확인
        Users usersEntity = usersRepository.findById(usersDto.getUsersId())
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

        if (bCryptPasswordEncoder.matches(usersDto.getUsersPw(), usersEntity.getUsersPw())) {
            throw new BadCredentialsException("현재 비밀번호와 다른 비밀번호를 사용해야합니다.");
        }
    }
    public UserResponseDto defaultLogin(LoginRequestDto loginRequestDto, HttpServletResponse res) {

        try {
            //존재 여부 확인
            Users usersEntity = usersRepository.findByUsersEmail(loginRequestDto.getUserEmail())
                    .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

            //비밀번호 확인
            if (!bCryptPasswordEncoder.matches(loginRequestDto.getUserPassword(), usersEntity.getUsersPw())) {
                throw new BadCredentialsException("비밀번호가 맞지 않습니다.");
            }

            if (usersEntity.getUsersBan().equals("Y")) {
                throw new BadCredentialsException("정지된 회원입니다");
            }

            TokenDto tokenDto = jwtUtil.createAllToken(usersEntity.getUsersEmail());
            setHeader(res, tokenDto);
            redisTemplate.opsForValue().set("JWT_TOKEN:" + usersEntity.getUsersEmail(), tokenDto.getRefreshToken(),7 * 24 * 60 * 60 * 1000L, java.util.concurrent.TimeUnit.MINUTES);
            return new UserResponseDto(usersEntity);

        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
    public UserResponseDto kakaoLogin(String accessToken, HttpServletResponse res) throws JsonProcessingException {

        // 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        UsersDto usersDto = getKakaoUserInfo(accessToken);

        // 3. "카카오 사용자 정보"로 필요시 회원가입
        Users users = insertSocialMember(usersDto);

        // 4. 강제 로그인 처리
        forceLogin(users);

        TokenDto tokenDto = jwtUtil.createAllToken(users.getUsersEmail());
        setHeader(res, tokenDto);
        redisTemplate.opsForValue().set("JWT_TOKEN:" + users.getUsersEmail(), tokenDto.getRefreshToken(),7 * 24 * 60 * 60 * 1000L, java.util.concurrent.TimeUnit.MINUTES);

        return new UserResponseDto(users);
    }

    private void forceLogin(Users users) {
        PrincipalDetails principalDetails = new PrincipalDetails(users);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UsersDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        log.info(jsonNode.get("properties").get("nickname").toString().substring(1,1));
        Long id = jsonNode.get("id").asLong();
        log.info("id = " + id);

        String name = jsonNode.get("properties").get("nickname").toString();
        name = name.substring(1, name.length()-1);
        String pwd = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());

        return UsersDto.builder()
                .usersPw(pwd)
                .usersEmail(id.toString())
                .usersName(name)
                .usersId(id.toString())
                .usersRole("ROLE_USER")
                .oAuthProvider(OAuthProvider.KAKAO)
                .build();
    }

    public Users insertSocialMember(UsersDto usersDto) {

        boolean existEmail = usersRepository.existsUsersByUsersEmail(usersDto.getUsersEmail());

        //등록된 이메일이 있으면 그냥 로그인만 회원 정보는 안받음
        if (existEmail) {
            return usersRepository.findByUsersEmail(usersDto.getUsersEmail()).orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        } else {

            Users usersEntity = Users.builder()
                    .usersCode(usersDto.getUsersCode())
                    .usersPw(usersDto.getUsersPw())
                    .usersEmail(usersDto.getUsersEmail())
                    .usersName(usersDto.getUsersName())
                    .usersGender(usersDto.getUsersGender())
                    .usersRole(usersDto.getUsersRole())
                    .usersBan(usersDto.getUsersBan())
                    .oAuthProvider(usersDto.getOAuthProvider())
                    .usersId(usersDto.getUsersId())
                    .build();

            usersEntity.setUsersBan("N");

            Users savedMember = usersRepository.save(usersEntity);

            return savedMember;
        }
    }
    public UserResponseDto naverLogin(String code, String state, HttpServletResponse res) throws IOException {

        UsersDto usersDto = getNaverUserInfo(code, state);

        Users users = insertSocialMember(usersDto);

        // 4. 강제 로그인 처리
        forceLogin(users);

        TokenDto tokenDto = jwtUtil.createAllToken(users.getUsersEmail());
        setHeader(res, tokenDto);
        redisTemplate.opsForValue().set("JWT_TOKEN:" + users.getUsersEmail(), tokenDto.getRefreshToken(),7 * 24 * 60 * 60 * 1000L, java.util.concurrent.TimeUnit.MINUTES);

        return new UserResponseDto(users);
    }

    public UsersDto getNaverUserInfo(String code, String state) throws IOException {

        String codeReqURL = "https://nid.naver.com/oauth2.0/token";
        String tokenReqURL = "https://openapi.naver.com/v1/nid/me";

        // 코드를 네이버에 전달하여 엑세스 토큰 가져옴
        JsonElement tokenElement = jsonElement(codeReqURL, null, code, state);

        String access_Token = tokenElement.getAsJsonObject().get("access_token").getAsString();
        String refresh_token = tokenElement.getAsJsonObject().get("refresh_token").getAsString();

        // 엑세스 토큰을 네이버에 전달하여 유저정보 가져옴
        JsonElement userInfoElement = jsonElement(tokenReqURL, access_Token, null, null);

        String naverId = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                .getAsJsonObject().get("id"));
        String userEmail = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                .getAsJsonObject().get("email"));
        String userName = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                .getAsJsonObject().get("name"));
        String gender = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                .getAsJsonObject().get("gender"));
        String mobile = String.valueOf(userInfoElement.getAsJsonObject().get("response")
                .getAsJsonObject().get("mobile"));


        String pwd = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());

        naverId = naverId.substring(1, naverId.length() - 1);
        userEmail = userEmail.substring(1, userEmail.length() - 1);
        userName = userName.substring(1, userName.length() - 1);
        gender = gender.substring(1, gender.length() - 1).equals("M") ? "남자" : "여자";
        mobile = (mobile.substring(1, mobile.length() - 1));

        return UsersDto.builder()
                .usersPw(pwd)
                .usersId(naverId)
                .usersName(userName)
                .usersEmail(userEmail)
                .usersGender(gender)
                .usersRole("ROLE_USER")
                .oAuthProvider(OAuthProvider.NAVER)
                .build();
    }
    // 네이버에 요청해서 데이터 전달 받는 메소드
    public JsonElement jsonElement(String reqURL, String token, String code, String state) throws IOException {

        // 요청하는 URL 설정
        URL url = new URL(reqURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // POST 요청을 위해 기본값이 false인 setDoOutput을 true로
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // POST 요청에 필요한 데이터 저장 후 전송
        if (token == null) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            String sb = "grant_type=authorization_code" +
                    "&client_id=" + naverClientId +
                    "&client_secret=" + naverClientSecret +
                    "&redirect_uri=" + naverRedirectUri +
                    "&code=" + code +
                    "&state=" + state;
            bw.write(sb);
            bw.flush();
            bw.close();
        } else {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();

        while ((line = br.readLine()) != null) {
            result.append(line);
        }
        br.close();

        // Gson 라이브러리에 포함된 클래스로 JSON 파싱
        return JsonParser.parseString(result.toString());
    }
    public String getNaverLoginReqUrl(HttpServletRequest request) {
        String clientId = naverClientId;
        String redirectUri = naverRedirectUri;
        String state = generateState();

        String reqUrl = "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        request.getSession().setAttribute("state", state);

        return reqUrl;
    }
    public String generateState() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }


    public UserResponseDto googleLogin(String code, HttpServletResponse res) throws JsonProcessingException {

        UsersDto usersDto = getGoogleUserInfo(code);

        Users users = insertSocialMember(usersDto);

        forceLogin(users);

        TokenDto tokenDto = jwtUtil.createAllToken(users.getUsersEmail());
        setHeader(res, tokenDto);
        redisTemplate.opsForValue().set("JWT_TOKEN:" + users.getUsersEmail(), tokenDto.getRefreshToken(),7 * 24 * 60 * 60 * 1000L, java.util.concurrent.TimeUnit.MINUTES);

        return new UserResponseDto(users);
    }

    public UsersDto getGoogleUserInfo(String code) {

        RestTemplate restTemplate = new RestTemplate();
        String codeReqURL = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", googleClientId);
        map.add("client_secret", googleClientSecret);
        map.add("redirect_uri", "https://ai.otoo.kr/googlelogin");
        map.add("grant_type", "authorization_code");
        map.add("code", code);


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<GoogleOAuthResponseDto> response = restTemplate.postForEntity(codeReqURL, request, GoogleOAuthResponseDto.class);
            GoogleOAuthResponseDto oauthResponse = response.getBody();
            String accessToken = oauthResponse.getAccess_token();

            return getGooleUserInfoByToken(accessToken);

        } catch (Exception e) {
            log.error("error =" + e.getMessage());
        }
        return null;
    }

    private UsersDto getGooleUserInfoByToken(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/userinfo/v2/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Bearer 토큰 설정

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        try {
            // Google 사용자 정보 요청
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> userInfo = response.getBody();

            String id = userInfo.get("id").toString();
            String name =  userInfo.get("name").toString();
            String email = userInfo.get("email").toString();
            String pwd = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());
            String gender = "비공개";

            return UsersDto.builder()
                    .usersId(id)
                    .usersPw(pwd)
                    .usersName(name)
                    .usersEmail(email)
                    .usersGender(gender)
                    .usersRole("ROLE_USER")
                    .oAuthProvider(OAuthProvider.GOOGLE) // 이 부분도 Google에 맞게 수정
                    .build();

        } catch (Exception e) {
            log.error("Error fetching Google user info: ", e.getMessage());
            return null;
        }

    }
    public void logout(String email) {
        // Redis에서 리프레시 토큰 삭제
        redisTemplate.delete("JWT_TOKEN:" + email);
    }

}
