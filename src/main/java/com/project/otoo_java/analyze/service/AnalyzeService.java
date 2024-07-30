package com.project.otoo_java.analyze.service;

import com.project.otoo_java.analyze.dto.AnalyzeDto;
import com.project.otoo_java.analyze.entity.Talks;
import com.project.otoo_java.analyze.repository.AnalyzeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class AnalyzeService {
    private final AnalyzeRepository analyzeRepository;

    public void insertAnalyze(AnalyzeDto analyzeDto) {
        Talks entity = AnalyzeDto.toEntity(analyzeDto);
        analyzeRepository.save(entity);
    }

    // 전체 회원의 분석결과 전체 가져오기 - 관리자용
    public List<Talks> getAllResult(){
        return analyzeRepository.findAll();
    }

    // 특정 회원의 분석결과 전체 가져오기 - 일반회원용
    public List<Talks> getUserResultAll(String usersCode){
        return analyzeRepository.findAllByUsersCode(usersCode);
    }

    // 특정 회원의 분석결과 1개 가져오기 - 일반회원용
    public Optional<Talks> getUserResultOne(Long talksCode){
        return analyzeRepository.findById(talksCode);
    }
}
