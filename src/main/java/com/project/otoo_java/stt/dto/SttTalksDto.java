package com.project.otoo_java.stt.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttTalksDto {

    private Long SttTalksCode;
    private Date SttTalksDate;
    private String SttTalksMessage;
    private String SttTalksResult;
    private String SttUsersCode;

}


