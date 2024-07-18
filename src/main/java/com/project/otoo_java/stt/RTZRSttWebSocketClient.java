package com.project.otoo_java.stt;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class RTZRSttWebSocketClient {

    private WebSocket rtzrWebSocket;
    private static final Logger logger = Logger.getLogger(RTZRSttWebSocketClient.class.getName());

    public RTZRSttWebSocketClient() throws Exception {
        OkHttpClient client = new OkHttpClient();
        String token = getAccessToken();

        HttpUrl.Builder httpBuilder = HttpUrl.get("https://openapi.vito.ai/v1/transcribe:streaming").newBuilder();
        httpBuilder.addQueryParameter("sample_rate", "8000");
        httpBuilder.addQueryParameter("encoding", "LINEAR16");
        httpBuilder.addQueryParameter("use_itn", "true");
        httpBuilder.addQueryParameter("use_disfluency_filter", "true");
        httpBuilder.addQueryParameter("use_profanity_filter", "false");

        String url = httpBuilder.toString().replace("https://", "wss://");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        RTZRWebSocketListener webSocketListener = new RTZRWebSocketListener();
        rtzrWebSocket = client.newWebSocket(request, webSocketListener);
    }

    // 마이크 입력 스트림을 위한 AudioInputStream을 설정합니다.
    private static AudioInputStream getAudioStream() throws Exception {
        AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(format);
        targetLine.start();
        return new AudioInputStream(targetLine);
    }

    public static String getAccessToken() throws IOException {
        OkHttpClient client = new OkHttpClient();
        String clientId = System.getenv("VITO_CLIENT_ID");
        String clientSecret = System.getenv("VITO_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Environment variables VITO_CLIENT_ID and VITO_CLIENT_SECRET must be set.");
        }

        RequestBody formBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url("https://openapi.vito.ai/v1/authenticate")
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "null";
            System.err.println("Authentication failed: " + errorBody);
            throw new IOException("Unexpected code " + response + " with body " + errorBody);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, String> map = objectMapper.readValue(response.body().string(), HashMap.class);

        return map.get("access_token");
    }


    public void send(byte[] message) {
        if (rtzrWebSocket != null) {
            rtzrWebSocket.send(ByteString.of(message));
        }
    }

    public void close() {
        if (rtzrWebSocket != null) {
            rtzrWebSocket.close(1000, "Session closed by client");
        }
    }

    public static void main(String[] args) throws Exception {
        RTZRSttWebSocketClient client = new RTZRSttWebSocketClient();

        // 마이크로부터 오디오 스트림을 가져옵니다.
        AudioInputStream audioStream = getAudioStream();

        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = audioStream.read(buffer)) != -1) {
            client.send(buffer);
        }
        client.close();

        audioStream.close();
    }
}

class RTZRWebSocketListener extends WebSocketListener {
    private static final Logger logger = Logger.getLogger(RTZRWebSocketListener.class.getName());
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private CountDownLatch latch = new CountDownLatch(1);

    private static void log(Level level, String msg, Object... args) {
        logger.log(level, msg, args);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log(Level.INFO, "Open " + response.message());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println(bytes.hex());
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

