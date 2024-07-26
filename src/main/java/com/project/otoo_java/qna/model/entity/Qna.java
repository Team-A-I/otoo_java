package com.project.otoo_java.qna.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Qna {
    @Id
    private String embeddingType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String qnaScript;

}
