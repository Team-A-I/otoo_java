package com.project.otoo_java.analyze.entity;

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
public class Talks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long talksCode;
    private Date talksDate;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String talksMessage;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String talksResult;
    private String talksType;
    private String talksPlayer;
    private String usersCode;

    @PrePersist
    protected void onCreate() {
        if (this.talksDate == null) {
            this.talksDate = new Date();
        }
    }
}
