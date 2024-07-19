package com.project.otoo_java.stt;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.logging.Logger;

public class AudioWebSocketHandler extends BinaryWebSocketHandler {
    private static final Logger logger = Logger.getLogger(AudioWebSocketHandler.class.getName());
    private VitoSttWebSocketClient vitoSttWebSocketClient;
    private WebSocketSession clientSession;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established");
        this.clientSession = session;
        vitoSttWebSocketClient = new VitoSttWebSocketClient(new VitoWebSocketListener() {
            @Override
            public void onMessage(String text) {
                try {
                    clientSession.sendMessage(new TextMessage(text));
                } catch (Exception e) {
                    logger.severe("Error sending message to client: " + e.getMessage());
                }
            }
        });
        vitoSttWebSocketClient.connect();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.info("Received binary message");
        byte[] data = message.getPayload().array();
        logger.info("Sending data to Vito: " + data.length + " bytes");
        if (vitoSttWebSocketClient != null) {
            vitoSttWebSocketClient.send(data);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed");
        if (vitoSttWebSocketClient != null) {
            vitoSttWebSocketClient.close();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.severe("WebSocket transport error: " + exception.getMessage());
        if (vitoSttWebSocketClient != null) {
            vitoSttWebSocketClient.close();
        }
    }
}
