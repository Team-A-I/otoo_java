package com.project.otoo_java.websocket;

import com.project.otoo_java.stream.TranscribeService;
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
import java.io.IOException;
import java.io.InputStream;

@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    private final TranscribeService transcribeService;
    private ByteArrayOutputStream byteArrayOutputStream;
    private WebSocketSession currentSession;
    private Process ffmpegProcess;
    private InputStream ffmpegInputStream;

    @Autowired
    public AudioStreamHandler(TranscribeService transcribeService) {
        this.transcribeService = transcribeService;
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] payload = message.getPayload().array();
        System.out.println("Received audio data of length: " + payload.length);
        byteArrayOutputStream.write(payload);

        // Convert webm to wav using ffmpeg process
        if (ffmpegProcess == null) {
            startFfmpegProcess();
        }

        ffmpegProcess.getOutputStream().write(payload);
        ffmpegProcess.getOutputStream().flush();

        // Read the converted data
        byte[] convertedData = new byte[1024];
        int bytesRead;
        while ((bytesRead = ffmpegInputStream.read(convertedData)) != -1) {
            ByteArrayInputStream convertedStream = new ByteArrayInputStream(convertedData, 0, bytesRead);
            AudioInputStream audioInputStream = new AudioInputStream(convertedStream, getAudioFormat(), bytesRead / getAudioFormat().getFrameSize());
            transcribeService.transcribe(audioInputStream, session);
        }
    }

    private void startFfmpegProcess() throws IOException {
        // ffmpeg 실행 파일의 절대 경로를 지정
        String ffmpegPath = "C:\\Users\\bjh73\\Downloads\\ffmpeg-master-latest-win64-gpl\\ffmpeg-master-latest-win64-gpl\\bin\\ffmpeg.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath, "-i", "pipe:0", "-f", "wav", "-ac", "1", "-ar", "16000", "pipe:1");
        processBuilder.redirectErrorStream(true);
        ffmpegProcess = processBuilder.start();
        ffmpegInputStream = ffmpegProcess.getInputStream();
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(16000, 16, 1, true, false);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        byteArrayOutputStream.reset();
        if (ffmpegProcess != null) {
            ffmpegProcess.destroy();
        }
        ffmpegProcess = null;
    }
}
