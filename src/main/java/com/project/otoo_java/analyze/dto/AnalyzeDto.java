package com.project.otoo_java.analyze.dto;

import com.project.otoo_java.analyze.entity.Talks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeDto {
    private Long talksCode;
    private Date talksDate;
    private String talksMessage;
    private String talksResult;
    private String talksType;
    private String talksPlayer;
    private String usersCode;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("talksCode", talksCode);
        map.put("talksDate", talksDate);
        map.put("talksMessage", talksMessage);
        map.put("talksResult", talksResult);
        map.put("talksType", talksType);
        map.put("talksPlayer", talksPlayer);
        map.put("usersCode", usersCode);
        return map;
    }

    public static Talks toEntity(AnalyzeDto dto) {
        return Talks.builder()
                .talksCode(dto.getTalksCode())
                .talksDate(dto.getTalksDate())
                .talksMessage(dto.getTalksMessage())
                .talksResult(dto.getTalksResult())
                .talksType(dto.getTalksType())
                .talksPlayer(dto.getTalksPlayer())
                .usersCode(dto.getUsersCode())
                .build();
    }
}
