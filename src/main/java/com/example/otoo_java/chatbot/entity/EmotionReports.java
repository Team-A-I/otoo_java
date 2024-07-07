package com.example.otoo_java.chatbot.entity;

import com.example.otoo_java.users.model.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class EmotionReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emotionReportCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String emotionReportEng;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String emotionReportKor;

    private Date emotionReportDate;
    private String emotionReportTitle;

    @ManyToOne
    @JoinColumn(name = "users_code") // 외래 키 설정
    private Users users;
}
