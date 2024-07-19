package com.project.otoo_java.stt;

import okhttp3.*;
import okio.ByteString;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VitoSttWebSocketClient {
    private static final Logger logger = Logger.getLogger(VitoSttWebSocketClient.class.getName());
    private WebSocket vitoWebSocket;
    private OkHttpClient client;
    private VitoWebSocketListener vitoWebSocketListener;

    public VitoSttWebSocketClient(VitoWebSocketListener listener) {
        this.vitoWebSocketListener = listener;
    }

    public void connect() throws IOException {
        client = new OkHttpClient();
        String token = Auth.getAccessToken();

        HttpUrl.Builder httpBuilder = HttpUrl.get("https://openapi.vito.ai/v1/transcribe:streaming").newBuilder();
        httpBuilder.addQueryParameter("sample_rate", "16000"); // 권장 샘플 레이트
        httpBuilder.addQueryParameter("encoding", "LINEAR16");
        httpBuilder.addQueryParameter("use_itn", "true");
        httpBuilder.addQueryParameter("use_disfluency_filter", "true");
        httpBuilder.addQueryParameter("use_profanity_filter", "true");
        httpBuilder.addQueryParameter("model_name", "sommers_ko"); // 한국어 모델 사용

        String url = httpBuilder.toString().replace("https://", "wss://");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        vitoWebSocket = client.newWebSocket(request, vitoWebSocketListener);
        client.dispatcher().executorService().shutdown();
    }

    public void send(byte[] data) {
        if (vitoWebSocket != null) {
            vitoWebSocket.send(ByteString.of(data));
        } else {
            logger.severe("WebSocket is not open");
        }
    }

    public void close() {
        if (vitoWebSocket != null) {
            vitoWebSocket.close(1000, "Closing");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}

abstract class VitoWebSocketListener extends WebSocketListener {
    private static final Logger logger = Logger.getLogger(VitoWebSocketListener.class.getName());

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        logger.info("Received message from Vito: " + text);
        onMessage(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String text = bytes.utf8();
        logger.info("Received binary message from Vito: " + text);
        onMessage(text);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        logger.severe("WebSocket failure: " + t.getMessage());
        if (response != null) {
            try {
                logger.severe("Response: " + response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        logger.info("WebSocket opened: " + response.message());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        logger.info("WebSocket closing: " + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        logger.info("WebSocket closed: " + reason);
    }

    public abstract void onMessage(String text);
}