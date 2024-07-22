package com.project.otoo_java.stream;

import ai.vito.openapi.v1.DecoderConfig;
import ai.vito.openapi.v1.DecoderRequest;
import ai.vito.openapi.v1.DecoderResponse;
import ai.vito.openapi.v1.OnlineDecoderGrpc;
import com.google.protobuf.ByteString;
import com.project.otoo_java.auth.Auth;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class VitoSttGrpcClient {
    private static final Logger logger = Logger.getLogger(VitoSttGrpcClient.class.getName());

    private OnlineDecoderGrpc.OnlineDecoderStub asyncStub;
    private StreamObserver<DecoderRequest> decoder;
    private CountDownLatch finishLatch;
    private String token;

    @PostConstruct
    public void init() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("grpc-openapi.vito.ai:443")
                .useTransportSecurity()
                .build();

        token = Auth.getAccessToken();

        asyncStub = OnlineDecoderGrpc.newStub(channel)
                .withCallCredentials(new CallCredentials() {
                    @Override
                    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                        final Metadata metadata = new Metadata();
                        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + token);
                        applier.apply(metadata);
                    }

                    @Override
                    public void thisUsesUnstableApi() {

                    }
                });

        finishLatch = new CountDownLatch(1);
    }

    public void startStreaming(AudioInputStream audioInputStream, StreamObserver<DecoderResponse> responseObserver) throws Exception {
        log(Level.INFO, "Starting streaming to STT service");
        decoder = asyncStub.decode(new StreamObserver<DecoderResponse>() {
            @Override
            public void onNext(DecoderResponse value) {
                log(Level.INFO, "Received response from STT service: " + value.toString());  // 추가된 로그
                responseObserver.onNext(value);
            }

            @Override
            public void onError(Throwable t) {
                log(Level.WARNING, "Error during streaming to STT service", t);
                responseObserver.onError(t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log(Level.INFO, "Streaming to STT service complete");
                responseObserver.onCompleted();
                finishLatch.countDown();
            }
        });

        DecoderConfig config = DecoderConfig.newBuilder()
                .setSampleRate(16000)
                .setEncoding(DecoderConfig.AudioEncoding.LINEAR16)
                .setUseItn(true)
                .setUseDisfluencyFilter(true)
                .setUseProfanityFilter(true)
                .build();
        setDecoderConfig(config);

        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = audioInputStream.read(buffer)) != -1) {
            send(buffer, readBytes);
        }
        closeSend();
    }

    public void setDecoderConfig(DecoderConfig config) {
        decoder.onNext(DecoderRequest.newBuilder().setStreamingConfig(config).build());
    }

    public void send(byte[] buff, int size) {
        decoder.onNext(DecoderRequest.newBuilder().setAudioContent(ByteString.copyFrom(buff, 0, size)).build());
    }

    public void closeSend() {
        decoder.onCompleted();
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        finishLatch.await(timeout, unit);
    }

    public void await() throws InterruptedException {
        finishLatch.await();
    }

    static void log(Level level, String msg, Object... args) {
        logger.log(level, msg, args);
    }
}
