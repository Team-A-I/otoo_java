package com.project.otoo_java.chatbot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionReportsDto {
    private Long emotionReportCode;
    private String emotionReportEng;
    private String emotionReportKor;
    private Date emotionReportDate;
    private String emotionReportTitle;
    private String usersCode;


}
