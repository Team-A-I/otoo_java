package com.project.otoo_java.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class TranscribeController extends BinaryWebSocketHandler {

    @Autowired
    private TranscribeService transcribeService;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, true);

    private WebSocketSession currentSession;

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] payload = message.getPayload().array();
        System.out.println("Received audio data of length: " + payload.length);

        // Append audio data to the byte array output stream
        byteArrayOutputStream.write(payload);

        // Convert the byte array output stream to an audio input stream
        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                audioFormat,
                byteArrayOutputStream.size()
        );

        // Send the audio input stream to the TranscribeService for transcription
        transcribeService.transcribe(audioInputStream, session);

        // Clear the byte array output stream for the next chunk of audio data
        byteArrayOutputStream.reset();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.currentSession = session;
        System.out.println("WebSocket connection established");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        this.currentSession = null;
        System.out.println("WebSocket connection closed");
        // Reset the byte array output stream when the WebSocket connection is closed
        byteArrayOutputStream.reset();
    }
}
