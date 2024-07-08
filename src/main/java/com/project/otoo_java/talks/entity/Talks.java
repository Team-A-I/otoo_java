package com.project.otoo_java.talks.entity;

import com.project.otoo_java.users.model.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Talks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long talksCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String talksMessage;
    private Date talksDate;
    private String talksPlayer;

    @ManyToOne
    @JoinColumn(name = "users_code") //외래 키 설정
    private Users users;
}
