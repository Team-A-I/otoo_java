package com.project.otoo_java.ocr.dto;

import com.project.otoo_java.ocr.entity.OcrTalks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrDto {
    private Long OcrTalksCode;
    private Date OcrTalksDate;
    private String OcrTalksMessage;
    private String OcrTalksResult;
    private String OcrUsersCode;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("OcrTalksCode", OcrTalksCode);
        map.put("OcrTalksDate", OcrTalksDate);
        map.put("OcrTalksMessage", OcrTalksMessage);
        map.put("OcrTalksResult", OcrTalksResult);
        map.put("OcrUsersCode", OcrUsersCode);
        return map;
    }

    public static OcrTalks toEntity(OcrDto dto) {
        return OcrTalks.builder()
                .OcrTalksCode(dto.getOcrTalksCode())
                .OcrTalksDate(dto.getOcrTalksDate())
                .OcrTalksMessage(dto.getOcrTalksMessage())
                .OcrTalksResult(dto.getOcrTalksResult())
                .ocrUsersCode(dto.getOcrUsersCode())
                .build();
    }

}
