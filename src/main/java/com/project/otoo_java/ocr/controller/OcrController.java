package com.project.otoo_java.ocr.controller;

import com.project.otoo_java.analyze.service.AnalyzeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OcrController {
    @Value("${FASTAPI_URL}")
    private String FASTAPI_URL;
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);

    @PostMapping("/conflict/ocr")
    public ResponseEntity<String> ocrConflict(@RequestParam("file") MultipartFile file) {
        return sendPostRequestToFastAPI(file, "conflict");
    }

    @PostMapping("/love/ocr")
    public ResponseEntity<String> ocrLove(@RequestParam("file") MultipartFile file) {
        return sendPostRequestToFastAPI(file, "love");
    }

    @PostMapping("/friendship/ocr")
    public ResponseEntity<String> ocrFriendship(@RequestParam("file") MultipartFile file) {
        return sendPostRequestToFastAPI(file, "friendship");
    }

    private ResponseEntity<String> sendPostRequestToFastAPI(MultipartFile file, String type) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileResource);
        } catch (IOException e) {
            logger.error("파일 변환 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다.");
        }

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    FASTAPI_URL + "/ocr",
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
