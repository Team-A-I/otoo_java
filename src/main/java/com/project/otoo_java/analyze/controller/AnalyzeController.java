package com.project.otoo_java.analyze.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.otoo_java.analyze.dto.AnalyzeDto;
import com.project.otoo_java.analyze.entity.Talks;
import com.project.otoo_java.analyze.service.AnalyzeService;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    @Value("${FASTAPI_URL}")
    private String FASTAPI_URL;
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

    // 관리자가 일반 회원들이 진행한 모든 테스트 결과 조회
    @GetMapping("/admin/getAllAnalysis")
    public List<Talks> getAllAnalysis(){
        return analyzeService.getAllResult();
    }

    // 일반 유저가 진행한 모든 갈등테스트-텍스트 결과 전체 조회
    @GetMapping("/user/getOneAnalysisAll/{usersCode}")
    public List<Talks> getOneAnalysisAll(@PathVariable(value="usersCode") String usersCode){
        return analyzeService.getUserResultAll(usersCode);
    }

    // 일반 유저가 진행한 갈등테스트-텍스트 결과 1개 조회
    @GetMapping("/user/getOneAnalysis/{talksCode}")
    public Optional<Talks> getOneAnalysis(@PathVariable(value="talksCode") Long talksCode){
        return analyzeService.getUserResultOne(talksCode);
    }

    private ResponseEntity<String> sendPostRequestToFastAPI(Map<String, Object> jsonContent, String type) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON 요청 본문에 type을 추가
        jsonContent.put("type", type);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonContent, headers);

        try {
            String talksMessage = (String) jsonContent.get("text");
            String usersCode = (String) jsonContent.get("usercode");
            String talksType = (String) jsonContent.get("type");


            ResponseEntity<String> response = restTemplate.exchange(
                    FASTAPI_URL + "/analyze",
                    HttpMethod.POST,
                    request,
                    String.class
            );
            logger.info("FastAPI 응답 성공: {}", response.getStatusCode());
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
//                analyzeService.insertAnalyze(analyzeDto);
//            지금은 저장 안함 7/24
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
