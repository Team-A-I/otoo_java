package com.example.otoo_java.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @PostMapping
    public ResponseEntity<String> handleFileUpload(@RequestBody JsonNode jsonNode) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(jsonNode, headers);

            ResponseEntity<String> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, requestEntity, String.class);

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            // FastAPI 서버로부터 받은 오류 메시지를 출력합니다.
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
