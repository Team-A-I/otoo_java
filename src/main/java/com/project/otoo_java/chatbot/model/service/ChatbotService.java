package com.project.otoo_java.chatbot.model.service;
import com.project.otoo_java.chatbot.model.dto.EmotionReportsDto;
import com.project.otoo_java.chatbot.model.entity.EmotionReports;
import com.project.otoo_java.chatbot.model.repository.EmotionReportsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatbotService {

    private final EmotionReportsRepository emotionReportsRepository;

    public void insertEmotionReport(EmotionReportsDto emotionReportsDto){
        EmotionReports entity = EmotionReports.builder()
                .emotionReportKor(emotionReportsDto.getEmotionReportKor())
                .usersCode(emotionReportsDto.getUsersCode())
                .emotionReportTitle(emotionReportsDto.getEmotionReportTitle())
                .build();
        emotionReportsRepository.save(entity);
    }

    // 전체 회원의 분석결과 전체 가져오기 - 관리자용
    public List<EmotionReports> getAllResult(){
        return emotionReportsRepository.findAll();
    }

    // 특정 회원의 분석결과 전체 가져오기 - 일반회원용
    public List<EmotionReports> getUserResultAll(String usersCode){
        return emotionReportsRepository.findAllByUsersCode(usersCode);
    }

    // 특정 회원의 분석결과 1개 가져오기 - 일반회원용
    public Optional<EmotionReports> getUserResultOne(Long emotionReportCode){
        return emotionReportsRepository.findById(emotionReportCode);
    }

}
