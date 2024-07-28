package com.project.otoo_java.analyze.service;

import com.project.otoo_java.analyze.dto.AnalyzeDto;
import com.project.otoo_java.analyze.entity.Talks;
import com.project.otoo_java.analyze.repository.AnalyzeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
//배포
@RequiredArgsConstructor
@Service
@Transactional
public class AnalyzeService {
    private final AnalyzeRepository analyzeRepository;

    public void insertAnalyze(AnalyzeDto analyzeDto) {
        Talks entity = AnalyzeDto.toEntity(analyzeDto);
        analyzeRepository.save(entity);
    }
}
