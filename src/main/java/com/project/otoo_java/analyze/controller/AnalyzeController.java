package com.project.otoo_java.analyze.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.otoo_java.analyze.dto.AnalyzeDto;
import com.project.otoo_java.analyze.service.AnalyzeService;
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

import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    private static final String FASTAPI_URL = "http://localhost:8001/analyze";
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeController.class);

    public AnalyzeController(AnalyzeService analyzeService) { // 생성자 추가
        this.analyzeService = analyzeService;
    }

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

        jsonContent.put("type", type);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonContent, headers);

        try {
            String talksMessage = (String) jsonContent.get("text");
            String usersCode = (String) jsonContent.get("usercode");
            String talksType = (String) jsonContent.get("type");
            ResponseEntity<String> response = restTemplate.exchange(
                    FASTAPI_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            logger.info("FastAPI 응답 성공: {}", response.getStatusCode());
            if (usersCode != null) {
                AnalyzeDto analyzeDto = new AnalyzeDto();
                analyzeDto.setUsersCode(usersCode);
                analyzeDto.setTalksMessage(talksMessage);
                analyzeDto.setTalksType(talksType);

                // JSON 응답에서 response 값을 추출
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                // response 내용을 다시 파싱
                String responseContent = jsonResponse.path("response").asText();
                JsonNode parsedResponse = objectMapper.readTree(responseContent.replace("```json\n", "").replace("```", ""));
                JsonNode totalScoreNode = parsedResponse.path("total_score");
                Iterator<String> fieldNames = totalScoreNode.fieldNames();
                String player1 = fieldNames.hasNext() ? fieldNames.next() : null;
                String player2 = fieldNames.hasNext() ? fieldNames.next() : null;

                analyzeDto.setTalksPlayer(player1 + ", " + player2);
                analyzeDto.setTalksResult(parsedResponse.toString());
                analyzeService.insertAnalyze(analyzeDto);
            }
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
