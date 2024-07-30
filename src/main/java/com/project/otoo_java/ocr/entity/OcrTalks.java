package com.project.otoo_java.ocr.entity;

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
public class OcrTalks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long OcrTalksCode;
    private Date OcrTalksDate;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String OcrTalksMessage;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String OcrTalksResult;
    private String ocrUsersCode;

    @PrePersist
    protected void onCreate() {
        if (this.OcrTalksDate == null) {
            this.OcrTalksDate = new Date();
        }
    }
}
