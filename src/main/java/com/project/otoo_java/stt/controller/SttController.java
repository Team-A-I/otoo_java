package com.project.otoo_java.stt.controller;

import com.project.otoo_java.stt.service.SttService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class SttController {
    private final SttService sttService;

    public SttController(SttService sttService) {
        this.sttService = sttService;
    }


    @PostMapping(value = "/transcribe/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> transcribeFile(@RequestPart MultipartFile file) throws IOException, InterruptedException {
        return sttService.transcribeFile(file);
    }

    @PostMapping(value = "/transcribe/websocket", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void transcribeWebsocketFile(@RequestPart MultipartFile file) throws IOException, UnsupportedAudioFileException, InterruptedException {
        sttService.transcribeWebSocketFile(file);
    }
}
