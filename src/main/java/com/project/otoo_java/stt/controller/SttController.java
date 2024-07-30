package com.project.otoo_java.stt.controller;

import com.project.otoo_java.stt.entity.SttTalks;
import com.project.otoo_java.stt.service.SttService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SttController {
    private final SttService sttService;

    public SttController(SttService sttService) {
        this.sttService = sttService;
    }

    @PostMapping(value = "/transcribe/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> transcribeFile(@RequestPart("file") MultipartFile file, @RequestPart(value = "usercode", required = false) String usercode) throws IOException, InterruptedException {
        return sttService.transcribeFile(file, usercode);
    }

    @PostMapping(value = "/transcribe/websocket", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void transcribeWebsocketFile(@RequestPart MultipartFile file) throws IOException, UnsupportedAudioFileException, InterruptedException {
        sttService.transcribeWebSocketFile(file);
    }

    // 관리자가 일반 회원들이 진행한 모든 테스트 결과 조회
    @GetMapping("/admin/getAllStt")
    public List<SttTalks> getAllStt(){
        return sttService.getAllResult();
    }

    // 일반 유저가 진행한 모든 갈등테스트-음성 결과 조회
    @GetMapping("/user/getOneSttAll/{sttUsersCode}")
    public List<SttTalks> getOneSttAll(@PathVariable(value="sttUsersCode") String sttUsersCode){
        return sttService.getUserResultAll(sttUsersCode);
    }

    // 일반 유저가 진행한 갈등테스트-음성 결과 1개 조회
    @GetMapping("/user/getOneStt/{sttTalksCode}")
    public Optional<SttTalks> getOneStt(@PathVariable(value="sttTalksCode") Long sttTalksCode){
        return sttService.getUserResultOne(sttTalksCode);
    }
}
