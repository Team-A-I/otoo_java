package com.project.otoo_java.analyze.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private static final String FASTAPI_URL = "http://python-fastapi:8001/analyze";
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeController.class);

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

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    FASTAPI_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            logger.info("FastAPI 응답 성공: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            logger.error("클라이언트 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body("요청 처리 중 오류가 발생했습니다: " + e.getStatusText());
        } catch (HttpServerErrorException e) {
            logger.error("서버 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body("FastAPI 서버에서 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            logger.error("리소스 접근 오류: {}", e.getMessage());
            return ResponseEntity.status(503).body("FastAPI 서버에 연결할 수 없습니다.");
        } catch (Exception e) {
            logger.error("예상치 못한 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body("서버 내부 오류가 발생했습니다.");
        }
    }
}
