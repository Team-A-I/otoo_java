package com.project.otoo_java.ocr.service;

import com.project.otoo_java.ocr.dto.OcrDto;
import com.project.otoo_java.ocr.entity.OcrTalks;
import com.project.otoo_java.ocr.repository.OcrRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class OcrService {
    private final OcrRepository ocrRepository;

    public void insertOcr(OcrDto ocrDto) {
        OcrTalks entity = OcrDto.toEntity(ocrDto);
        ocrRepository.save(entity);
    }
}
