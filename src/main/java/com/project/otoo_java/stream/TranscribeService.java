package com.project.otoo_java.stream;

import ai.vito.openapi.v1.DecoderResponse;
import ai.vito.openapi.v1.SpeechRecognitionAlternative;
import ai.vito.openapi.v1.StreamingRecognitionResult;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.sound.sampled.AudioInputStream;
import java.util.logging.Level;

import static com.project.otoo_java.stream.VitoSttGrpcClient.log;

@Service
public class TranscribeService {

    @Autowired
    private VitoSttGrpcClient vitoSttGrpcClient;

    public void transcribe(AudioInputStream audioInputStream, WebSocketSession session) {
        try {
            log(Level.INFO, "Starting transcription service");
            vitoSttGrpcClient.startStreaming(audioInputStream, new StreamObserver<DecoderResponse>() {
                @Override
                public void onNext(DecoderResponse value) {
                    log(Level.INFO, "Received response from STT service: " + value.toString());
                    if (value.getResultsCount() > 0) {
                        StreamingRecognitionResult result = value.getResults(0);
                        if (result.getAlternativesCount() > 0) {
                            SpeechRecognitionAlternative best = result.getAlternatives(0);
                            String transcript = best.getText();
                            log(Level.INFO, "Received transcript: " + transcript);
                            if (result.getIsFinal()) {
                                System.out.printf("final:%6d,%6d: %s\n", result.getStartAt(), result.getDuration(), transcript);
                            } else {
                                System.out.printf(transcript + "\n");
                            }
                            try {
                                session.sendMessage(new TextMessage(transcript)); // 클라이언트로 텍스트 메시지 전송
                            } catch (Exception e) {
                                log(Level.WARNING, "Error sending message to WebSocket", e);
                            }
                        } else {
                            log(Level.WARNING, "No alternatives in the result");
                        }
                    } else {
                        log(Level.WARNING, "No results in the response");
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log(Level.WARNING, "on error", t);
                }

                @Override
                public void onCompleted() {
                    log(Level.INFO, "Complete");
                }
            });
        } catch (Exception e) {
            log(Level.SEVERE, "Exception in transcription service", e);
        }
    }
}


