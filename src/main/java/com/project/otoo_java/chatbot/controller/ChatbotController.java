package com.project.otoo_java.chatbot.controller;
import com.google.api.Authentication;
import com.project.otoo_java.chatbot.model.dto.EmotionReportsDto;
import com.project.otoo_java.chatbot.model.entity.EmotionReports;
import com.project.otoo_java.chatbot.model.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class ChatbotController {
    private final ChatbotService chatbotService;

    @Value("${FASTAPI_URL}")
    private String FASTAPI_URL;


    @PostMapping("/chatbot")
    public ResponseEntity<String> chatbot(@RequestParam(required = false) String userCode, @RequestBody Map<String, Object> payload) {
        try {
            List recentMessages = (List) payload.get("RecentMessages");
            String mode = (String) payload.get("mode");
            String url = FASTAPI_URL + "/chatbot";;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> recentMessagesRequest = new HashMap<>();
            recentMessagesRequest.put("RecentMessages", recentMessages);
            Map<String, Object> modeRequest = new HashMap<>();
            modeRequest.put("mode", mode);
            Map<String, Object> fullRequest = new HashMap<>();
            fullRequest.put("recent_messages_request", recentMessagesRequest);
            fullRequest.put("mode_request", modeRequest);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fullRequest, headers);
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
    @PostMapping("/emotionReport")
    public ResponseEntity<String> emotionReport(@RequestBody Map<String, Object> payload) {
        try {
            List messages = (List) payload.get("messages");
            String usersCode = (String) payload.get("usersCode");
            String url = FASTAPI_URL + "/emotionReport";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("messages", messages.toString());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("FastAPI 응답 성공: {}", response.getStatusCode());
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

    // 관리자가 일반 회원들이 진행한 모든 테스트 결과 조회
    @GetMapping("/admin/getAllChatbot")
    public List<EmotionReports> getAllChatbot(){
        return chatbotService.getAllResult();
    }

    // 일반 유저가 진행한 모든 챗봇 결과 조회
    @GetMapping("/user/getOneChatbotAll/{usersCode}")
    public List<EmotionReports> getOneChatbotAll(@PathVariable(value="usersCode") String usersCode){
        return chatbotService.getUserResultAll(usersCode);
    }

    // 일반 유저가 진행한 챗봇 결과 1개 조회
    @GetMapping("/user/getOneChatbot/{emotionReportCode}")
    public Optional<EmotionReports> getOneChatbot(
            @PathVariable(value="emotionReportCode")
            Long emotionReportCode){
        return chatbotService.getUserResultOne(emotionReportCode);
    }

}