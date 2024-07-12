package com.project.otoo_java.jwt;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.project.otoo_java.jwt.JwtUtil.BEARER_PREFIX;

@Setter
@Getter
@NoArgsConstructor
public class TokenDto {

    private String accessToken;
    private String refreshToken;

    public TokenDto(String accessToken, String refreshToken) {
        this.accessToken = BEARER_PREFIX + accessToken;
        this.refreshToken = BEARER_PREFIX + refreshToken;
    }

}