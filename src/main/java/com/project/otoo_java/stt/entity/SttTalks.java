package com.project.otoo_java.stt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SttTalks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SttTalksCode;
    private Date SttTalksDate;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String SttTalksMessage;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String SttTalksResult;
    private String sttUsersCode;

    @PrePersist
    protected void onCreate() {
        if (this.SttTalksDate == null) {
            this.SttTalksDate = new Date();
        }
    }
}

