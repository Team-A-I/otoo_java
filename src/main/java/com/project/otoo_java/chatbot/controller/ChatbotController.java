package com.project.otoo_java.chatbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class ChatbotController {
    @PostMapping("/chatbot")
    public String chatbot(@RequestParam(required = false) String user_id, @RequestBody Map<String, Object> payload) {
        List RecentMessages = (List) payload.get("RecentMessages");
        String mode = (String) payload.get("mode");
        String url = "http://python-fastapi:8001/chatbot";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> recentMessagesRequest = new HashMap<>();
        recentMessagesRequest.put("RecentMessages", RecentMessages);
        Map<String, Object> modeRequest = new HashMap<>();
        modeRequest.put("mode", mode);

        Map<String, Object> fullRequest = new HashMap<>();
        fullRequest.put("recent_messages_request",recentMessagesRequest);
        fullRequest.put("mode_request", modeRequest);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fullRequest, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }

    @PostMapping("/emotionReport")
    public String emotionReport(@RequestParam(required = false) String user_id, @RequestBody Map<String, Object> payload) {
        List messages = (List) payload.get("messages");
        log.info(messages.toString());
        String url = "http://python-fastapi:8001/emotionReport";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("messages", messages.toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);


        return response.getBody();
    }
}
