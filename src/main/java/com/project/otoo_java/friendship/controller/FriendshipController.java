package com.project.otoo_java.friendship.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> handleUpload(@RequestBody Map<String, Object> payload) {
        RestTemplate restTemplate = new RestTemplate();
        String pythonServerUrl = "http://localhost:8001/friendship";

        // 파이썬 서버로 전송
        ResponseEntity<String> response = restTemplate.postForEntity(pythonServerUrl, payload, String.class);

        // 응답을 Map으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseBody;
        try {
            responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }

        // 변환된 응답 반환
        return ResponseEntity.ok(responseBody);
    }
}
