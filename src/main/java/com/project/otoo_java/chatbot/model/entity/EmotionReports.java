package com.project.otoo_java.chatbot.model.entity;

import com.project.otoo_java.users.model.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
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

    private String usersCode;

    @PrePersist
    protected void onCreate() {
        if (this.emotionReportDate == null) {
            this.emotionReportDate = new Date();
        }
    }
}
