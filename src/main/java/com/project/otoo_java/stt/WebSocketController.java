package com.project.otoo_java.stt;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static RTZRSttWebSocketClient sttClient;

    static {
        try {
            sttClient = new RTZRSttWebSocketClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(byte[] message) throws Exception {
        // 받은 메시지의 크기를 로그로 출력
        System.out.println("Received data size: " + message.length);
        if (message.length > 0) {
            sttClient.send(message);
        }
        return new String(message);
    }

    @MessageMapping("/eos")
    public void handleEos(String message) {
        if ("EOS".equals(message) && sttClient != null) {
            sttClient.close();
        }
    }
}
