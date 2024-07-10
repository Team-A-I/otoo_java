package com.project.otoo_java.chatbot.model.service;
import com.project.otoo_java.chatbot.model.dto.EmotionReportsDto;
import com.project.otoo_java.chatbot.model.entity.EmotionReports;
import com.project.otoo_java.chatbot.model.repository.EmotionReportsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

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

}
