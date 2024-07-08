package com.project.otoo_java.users.model.dto;

import com.project.otoo_java.enums.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersDto {
    private String usersCode;
    private String usersId;
    private String usersPw;
    private String usersEmail;
    private String usersName;
    private String usersRole;
    private String usersBan;
    private String usersGender;
    private OAuthProvider oAuthProvider;
}
