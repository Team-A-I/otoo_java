package com.project.otoo_java.chatbot.controller;

import com.project.otoo_java.chatbot.model.dto.EmotionReportsDto;
import com.project.otoo_java.chatbot.model.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chatbot")
    public ResponseEntity<String> chatbot(@RequestParam(required = false) String userCode, @RequestBody Map<String, Object> payload) {
       try {
           List RecentMessages = (List) payload.get("RecentMessages");
           String mode = (String) payload.get("mode");
           String url = "http://localhost:8001/chatbot";
           HttpHeaders headers = new HttpHeaders();
           headers.setContentType(MediaType.APPLICATION_JSON);

           Map<String, Object> recentMessagesRequest = new HashMap<>();
           recentMessagesRequest.put("RecentMessages", RecentMessages);
           Map<String, Object> modeRequest = new HashMap<>();
           modeRequest.put("mode", mode);

           Map<String, Object> fullRequest = new HashMap<>();
           fullRequest.put("recent_messages_request", recentMessagesRequest);
           fullRequest.put("mode_request", modeRequest);

           HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fullRequest, headers);

           RestTemplate restTemplate = new RestTemplate();

           ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
           return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
       }catch (HttpClientErrorException e) {
           return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
       } catch (Exception e) {
            return new ResponseEntity<>("Failed to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PostMapping("/emotionReport")
    public ResponseEntity<String> emotionReport(@RequestBody Map<String, Object> payload) {
        try {
            List messages = (List) payload.get("messages");
            String usersCode = (String) payload.get("usersCode");
            String url = "http://localhost:8001/emotionReport";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("messages", messages.toString());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);


            if (usersCode != null) {
                EmotionReportsDto emotionReportsDto = new EmotionReportsDto();
                emotionReportsDto.setUsersCode(usersCode);
                emotionReportsDto.setEmotionReportKor(response.getBody().substring(1, response.getBody().length() - 1));

                if (response.getBody().length() > 11) {
                    emotionReportsDto.setEmotionReportTitle(response.getBody().substring(1, 11) + "...");
                } else {
                    emotionReportsDto.setEmotionReportTitle(response.getBody());
                }

                chatbotService.insertEmotionReport(emotionReportsDto);

            }

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
