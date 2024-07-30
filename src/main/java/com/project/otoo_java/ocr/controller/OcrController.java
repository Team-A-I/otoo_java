package com.project.otoo_java.ocr.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.otoo_java.ocr.dto.OcrDto;
import com.project.otoo_java.ocr.entity.OcrTalks;
import com.project.otoo_java.ocr.service.OcrService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class OcrController {

    private final OcrService ocrService;

    @Value("${FASTAPI_URL}")
    private String FASTAPI_URL;
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/conflict/ocr")
    public ResponseEntity<String> ocrConflict(@RequestParam("file") MultipartFile[] files, @RequestParam(value = "usercode", required = false) String usercode) {
        return sendPostRequestToFastAPI(files, "conflict", usercode);
    }

    @PostMapping("/love/ocr")
    public ResponseEntity<String> ocrLove(@RequestParam("file") MultipartFile[] files, @RequestParam(value = "usercode", required = false) String usercode) {
        return sendPostRequestToFastAPI(files, "love", usercode);
    }

    @PostMapping("/friendship/ocr")
    public ResponseEntity<String> ocrFriendship(@RequestParam("file") MultipartFile[] files, @RequestParam(value = "usercode", required = false) String usercode) {
        return sendPostRequestToFastAPI(files, "friendship", usercode);
    }

    // 관리자가 일반 회원들이 진행한 모든 테스트 결과 조회
    @GetMapping("/admin/getAllOcr")
    public List<OcrTalks> getAllOcr(){
        return ocrService.getAllResult();
    }

    // 일반 유저가 진행한 모든 갈등테스트(이미지) 전체 결과 조회
    @GetMapping("/user/getOneOcrAll/{ocrUsersCode}")
    public List<OcrTalks> getOneOcrAll(@PathVariable(value="ocrUsersCode") String ocrUsersCode){
        return ocrService.getUserResultAll(ocrUsersCode);
    }

    // 일반 유저가 진행한 갈등테스트(이미지) 결과 1개 조회 -> 결과 페이지 나타내기 + 시간 선택하면 조회
    @GetMapping("/user/getOneOcr/{ocrTalksCode}")
    public Optional<OcrTalks> getOneOcr(
            @PathVariable(value="ocrTalksCode")
            Long ocrTalksCode){
        return ocrService.getUserResultOne(ocrTalksCode);
    }

    private ResponseEntity<String> sendPostRequestToFastAPI(MultipartFile[] files, String type, String usercode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        if (usercode != null && !usercode.isEmpty()) {
            body.add("usercode", usercode);
        }
        try {
            for (MultipartFile file : files) {
                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("files", fileResource);
            }
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
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String responseContent = jsonResponse.path("response").asText();
            JsonNode parsedResponse = objectMapper.readTree(responseContent.replace("```json\n", "").replace("```", ""));

            OcrDto ocrDto = new OcrDto();
            ocrDto.setOcrUsersCode(usercode);
            ocrDto.setOcrTalksMessage(files.toString());
            ocrDto.setOcrTalksResult(parsedResponse.toString());

            // ocrService.insertOcr(ocrDto);
            // 지금은 저장 안함 7/26
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
