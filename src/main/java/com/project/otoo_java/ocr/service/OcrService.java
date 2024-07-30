package com.project.otoo_java.ocr.service;

import com.project.otoo_java.ocr.dto.OcrDto;
import com.project.otoo_java.ocr.entity.OcrTalks;
import com.project.otoo_java.ocr.repository.OcrRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class OcrService {
    private final OcrRepository ocrRepository;

    public void insertOcr(OcrDto ocrDto) {
        OcrTalks entity = OcrDto.toEntity(ocrDto);
        ocrRepository.save(entity);
    }

    // 전체 회원의 분석결과 전체 가져오기 - 관리자용
    public List<OcrTalks> getAllResult(){
        return ocrRepository.findAll();
    }

    // 특정 회원의 분석결과 전체 가져오기 - 일반회원용
    public List<OcrTalks> getUserResultAll(String OcrUsersCode){
        return ocrRepository.findByOcrUsersCode(OcrUsersCode);
    }

    // 특정 회원의 분석결과 1개 가져오기 - 일반회원용
    public Optional<OcrTalks> getUserResultOne(Long ocrTalksCode){
        return ocrRepository.findById(ocrTalksCode);
    }
}
