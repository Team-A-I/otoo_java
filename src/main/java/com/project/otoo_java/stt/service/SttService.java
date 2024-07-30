package com.project.otoo_java.stt.service;

import com.project.otoo_java.stt.dto.SttTalksDto;
import com.project.otoo_java.stt.entity.SttTalks;
import com.project.otoo_java.stt.repository.SttTalksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Slf4j
@RequiredArgsConstructor
public class SttService {

    private final SttTalksRepository sttTalksRepository;

    private boolean stopPolling = false;
    private String transcribeId = null;
    private String accessToken = null;

    @Value("${vito.client_id}")
    String client_id;

    @Value("${vito.client_secret}")
    String client_secret;

    @Value("${FASTAPI_URL}")
    String fastApiUrl;

    // 전체 회원의 분석결과 전체 가져오기 - 관리자용
    public List<SttTalks> getAllResult(){
        return sttTalksRepository.findAll();
    }

    // 특정 회원의 분석결과 전체 가져오기 - 일반회원용
    public List<SttTalks> getUserResultAll(String sttUsersCode){
        return sttTalksRepository.findAllBySttUsersCode(sttUsersCode);
    }

    // 특정 회원의 분석결과 1개 가져오기 - 일반회원용
    public Optional<SttTalks> getUserResultOne(Long sttTalksCode){
        return sttTalksRepository.findById(sttTalksCode);
    }

    public String getAccessToken() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai")
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", client_id);
        formData.add("client_secret", client_secret);

        String response = webClient
                .post()
                .uri("/v1/authenticate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info(response);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.getString("access_token");
    }

    public ResponseEntity<String> transcribeFile(MultipartFile multipartFile, String usercode) throws IOException, InterruptedException {
        accessToken = getAccessToken();
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.MULTIPART_FORM_DATA))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                .build();

        byte[] fileBytes = multipartFile.getBytes();
        ByteArrayResource byteArrayResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename();
            }
        };

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", byteArrayResource, MediaType.MULTIPART_FORM_DATA);
        multipartBodyBuilder.part("config", "{\"use_diarization\": true, \"diarization\": {\"spk_count\": 2}, \"domain\": \"GENERAL\"}", MediaType.APPLICATION_JSON);

        String response = null;
        try {
            response = webClient.post()
                    .uri("/transcribe")
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("post 끝");
        } catch (WebClientResponseException e) {
            log.error(String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "STT 요청 실패");
        }

        if (response != null) {
            JSONObject jsonObject = new JSONObject(response);

            try {
                if (jsonObject.getString("code").equals("H0002")) {
                    log.info("accessToken 만료로 재발급 받습니다");
                    accessToken = getAccessToken();
                    response = webClient.post()
                            .uri("/transcribe")
                            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                }
            } catch (JSONException e) {
                log.info("code 확인 불가 오류 catch");
                log.info(e.toString());
            }

            log.info("transcribe 요청 id : " + jsonObject.getString("id"));

            stopPolling = false;
            transcribeId = jsonObject.getString("id");
            String finalResponse = startPolling();

            JSONObject finalResponseJson = new JSONObject(finalResponse);

            if (!finalResponseJson.has("results") || !finalResponseJson.getJSONObject("results").has("utterances")) {
                log.error("STT 변환 결과에 utterances 키가 없습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "STT 변환 결과에 utterances 키가 없습니다.");
            }

            JSONArray utterances = finalResponseJson.getJSONObject("results").getJSONArray("utterances");
            StringBuilder transcribedText = new StringBuilder();

            for (int i = 0; i < utterances.length(); i++) {
                JSONObject utterance = utterances.getJSONObject(i);
                transcribedText.append(utterance.getString("msg")).append(" ");
            }

            if (transcribedText.length() < 30) {
                log.error("대화내용이 짧아서 분석할 수 없습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대화내용이 짧아서 분석할 수 없습니다.");
            }

            // JSON 응답을 파싱하여 'verified' 필드를 리스트로 변경
            JSONObject responseJson = new JSONObject(finalResponse);
            JSONObject results = responseJson.getJSONObject("results");

            // 'verified' 필드가 리스트 형식이 아닌 경우 리스트로 변경
            if (!results.has("verified") || !(results.get("verified") instanceof List)) {
                List<Boolean> verifiedList = new ArrayList<>();
                verifiedList.add(results.getBoolean("verified"));
                results.put("verified", verifiedList);
            }

            // FastAPI로 데이터 전송
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(responseJson.toString(), headers);
            ResponseEntity<String> fastApiResponse = restTemplate.postForEntity(fastApiUrl + "/stt", request, String.class);

            // FastAPI 응답을 로그로 출력
            log.info("FastAPI 응답: " + fastApiResponse.getStatusCode());
            log.info("FastAPI 응답 본문: " + fastApiResponse.getBody());

            // DTO에 데이터 저장
            SttTalksDto sttTalksDto = SttTalksDto.builder()
                    .SttUsersCode(usercode) // usercode가 null일 수 있음
                    .SttTalksMessage(transcribedText.toString())
                    .SttTalksResult(finalResponseJson.toString())
                    .build();

            // 데이터베이스에 저장
            insertSttTalks(sttTalksDto);

            return new ResponseEntity<>(fastApiResponse.getBody(), HttpStatus.OK);
        } else {
            log.error("STT 응답이 없습니다.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "STT 응답이 없습니다.");
        }
    }

    private void insertSttTalks(SttTalksDto sttTalksDto) {
        SttTalks entity = SttTalks.builder()
                .SttTalksMessage(sttTalksDto.getSttTalksMessage())
                .SttTalksResult(sttTalksDto.getSttTalksResult())
                .sttUsersCode(sttTalksDto.getSttUsersCode()) // null이 올 수 있음
                .build();
        sttTalksRepository.save(entity);
    }

    // 5초마다 실행 (주기는 필요에 따라 조절)
    public String startPolling() throws InterruptedException {
        log.info("Polling 함수 첫 시작");
        String response = null;
        Thread.sleep(5000);
        while (!stopPolling) {
            log.info("while polling 시작 반복중");
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openapi.vito.ai/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                    .build();

            String uri = "/transcribe/" + transcribeId;
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject jsonObject = new JSONObject(response);
            // status 확인하여 폴링 중단 여부 결정
            if (jsonObject.getString("status").equals("completed")) {
                stopPolling = true;
            }

            try {
                Thread.sleep(5000); // 폴링 주기 (5초)를 설정
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("while polling 끝 반복중");
        }

        log.info("폴링함수 끝");
        log.info(response);
        return response;
    }

    public void transcribeWebSocketFile(MultipartFile multipartFile) throws IOException, InterruptedException {
        Logger logger = Logger.getLogger(SttService.class.getName());
        OkHttpClient client = new OkHttpClient();
        String token = getAccessToken();

        HttpUrl.Builder httpBuilder = HttpUrl.get("https://openapi.vito.ai/v1/transcribe:streaming").newBuilder();
        httpBuilder.addQueryParameter("sample_rate", "44100");
        httpBuilder.addQueryParameter("encoding", "WAV");
        httpBuilder.addQueryParameter("use_diarization", "true");
        httpBuilder.addQueryParameter("spk_count", "2");
        httpBuilder.addQueryParameter("use_itn", "true");
        httpBuilder.addQueryParameter("use_disfluency_filter", "true");
        httpBuilder.addQueryParameter("use_profanity_filter", "true");

        String url = httpBuilder.toString().replace("https://", "wss://");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        VitoWebSocketListener webSocketListener = new VitoWebSocketListener();
        WebSocket vitoWebSocket = client.newWebSocket(request, webSocketListener);

        InputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes());
        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = inputStream.read(buffer)) != -1) {
            boolean sent = vitoWebSocket.send(ByteString.of(buffer, 0, readBytes));
            if (!sent) {
                logger.log(Level.WARNING, "Send buffer is full. Cannot complete request. Increase sleep interval.");
                System.exit(1);
            }
            Thread.sleep(0, 100);
        }
        inputStream.close();
        vitoWebSocket.send("EOS");

        webSocketListener.waitClose();
        client.dispatcher().executorService().shutdown();
    }
}

@Slf4j
class VitoWebSocketListener extends WebSocketListener {
    private static final Logger logger = Logger.getLogger(SttService.class.getName());
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private CountDownLatch latch = null;

    private static void log(Level level, String msg, Object... args) {
        logger.log(level, msg, args);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log(Level.INFO, "Open " + response.message());
        latch = new CountDownLatch(1);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println(text);
        log.info(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println(bytes.hex());
        log.info(bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        log(Level.INFO, "Closing {0} {1}", code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        log(Level.INFO, "Closed {0} {1}", code, reason);
        latch.countDown();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
        latch.countDown();
    }

    public void waitClose() throws InterruptedException {
        log(Level.INFO, "Wait for finish");
        latch.await();
    }
}
