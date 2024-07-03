package com.example.otoo_java.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Users {
    @Id
    private String usersId;
    private String usersPw;
    private String usersEmail;
    private String usersName;
    private String usersRole;
    private String usersBan;
}
