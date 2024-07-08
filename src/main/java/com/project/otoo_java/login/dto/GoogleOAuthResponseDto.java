package com.project.otoo_java.login.dto;

import lombok.Data;

@Data
public class GoogleOAuthResponseDto {
    private String access_token;
    private Integer expires_in;
    private String scope;
    private String token_type;
    private String id_token;
}
