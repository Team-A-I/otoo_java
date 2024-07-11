package com.project.otoo_java.feedback.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private Long feedbackCode;
    private Long feedbackLike;
    private Long feedbackDislike;
    private String feedbackType;
    private String feedbackNote;
}
