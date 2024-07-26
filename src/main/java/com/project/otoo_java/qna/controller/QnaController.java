package com.project.otoo_java.qna.controller;

import com.project.otoo_java.qna.model.dto.QnaDto;
import com.project.otoo_java.qna.model.entity.Qna;
import com.project.otoo_java.qna.model.repository.QnaRepository;
import com.project.otoo_java.qna.model.service.QnaService;
import com.project.otoo_java.users.model.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class QnaController {

    private final QnaService qnaService;

    @Value("${FASTAPI_URL}")
    private String FASTAPI_URL;

    @PostMapping("/qna")
    public ResponseEntity<String> qna(@RequestBody String chat) {
        try {
            String url = FASTAPI_URL + "/qna";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("messages", chat);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("FastAPI 응답 성공: {}", response.getStatusCode());
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.error("클라이언트 오류: {}", e.getMessage());
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("서버 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            log.error("리소스 접근 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에 연결할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage());
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/qna-edit")
    public ResponseEntity<String> insertQna(@RequestBody String qnaScript) {
        try {
            JSONObject jsonObject = new JSONObject(qnaScript);
            qnaScript = jsonObject.getString("qna");
            log.info(qnaScript);
            String embaddingType = "qna";
            QnaDto qnaDto = new QnaDto();
            qnaDto.setEmbeddingType(embaddingType);
            qnaDto.setQnaScript(qnaScript);
            qnaService.insertQna(qnaDto);
            String url = FASTAPI_URL + "/qna-edit";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("messages", qnaScript);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("FastAPI 응답 성공: {}", response.getStatusCode());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.error("클라이언트 오류: {}", e.getMessage());
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("서버 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            log.error("리소스 접근 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에 연결할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage());
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/admin/qna-view")
    public ResponseEntity<String> insertQna() {
        try {
            Qna qna = qnaService.selectQna("qna");
            return new ResponseEntity<>(qna.getQnaScript(), HttpStatus.OK);
        }catch (HttpClientErrorException e) {
            log.error("클라이언트 오류: {}", e.getMessage());
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("서버 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            log.error("리소스 접근 오류: {}", e.getMessage());
            return new ResponseEntity<>("FastAPI 서버에 연결할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage());
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
