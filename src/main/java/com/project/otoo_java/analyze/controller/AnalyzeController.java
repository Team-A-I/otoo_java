package com.project.otoo_java.analyze.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private static final String FASTAPI_URL = "http://localhost:8001/analyze";

    @PostMapping("/conflict/analysis")
    public ResponseEntity<String> analyzeConflict(@RequestBody Map<String, Object> jsonContent) {
        return sendPostRequestToFastAPI(jsonContent, "conflict");
    }

    @PostMapping("/love/analysis")
    public ResponseEntity<String> analyzeLove(@RequestBody Map<String, Object> jsonContent) {
        return sendPostRequestToFastAPI(jsonContent, "love");
    }

    @PostMapping("/friendship/analysis")
    public ResponseEntity<String> analyzeFriendship(@RequestBody Map<String, Object> jsonContent) {
        return sendPostRequestToFastAPI(jsonContent, "friendship");
    }

    private ResponseEntity<String> sendPostRequestToFastAPI(Map<String, Object> jsonContent, String type) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON 요청 본문에 type을 추가
        jsonContent.put("type", type);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonContent, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                FASTAPI_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        return response;
    }
}