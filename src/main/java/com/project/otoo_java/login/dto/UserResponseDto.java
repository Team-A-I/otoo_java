package com.project.otoo_java.login.dto;
import com.project.otoo_java.users.model.entity.Users;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserResponseDto {

    private String usersCode;

    private String userName;

    private String userEmail;

    private String role;
//
    public UserResponseDto(Users users) {
        this.usersCode = users.getUsersCode();
        this.userName = users.getUsersName();
        this.userEmail = users.getUsersEmail();
        this.role = users.getUsersRole();
    }
}