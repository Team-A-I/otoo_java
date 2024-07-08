package com.project.otoo_java.users.model.entity;

import com.project.otoo_java.enums.OAuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Users {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private String usersCode;

    private String usersId;

    private String usersPw;

    @Email
    private String usersEmail;

    private String usersName;

    private String usersRole;

    private String usersBan;

    private String usersGender;

    @Column(name="users_logintype")
    private OAuthProvider oAuthProvider;
}
