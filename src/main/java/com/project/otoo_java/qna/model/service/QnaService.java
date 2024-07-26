package com.project.otoo_java.qna.model.service;


import com.project.otoo_java.qna.model.dto.QnaDto;
import com.project.otoo_java.qna.model.entity.Qna;
import com.project.otoo_java.qna.model.repository.QnaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class QnaService {
    private final QnaRepository qnaRepository;

    public void insertQna(QnaDto qnaDto){
        Qna entity = Qna.builder()
                .qnaScript(qnaDto.getQnaScript())
                .embeddingType(qnaDto.getEmbeddingType())
                .build();
        qnaRepository.save(entity);
    }

    public Qna selectQna(String embeddingType){
        Qna qnaEntity = qnaRepository.findById(embeddingType).orElseThrow(() -> new IllegalArgumentException("작성된 QnA가 없습니다."));
        return Qna.builder()
                .qnaScript(qnaEntity.getQnaScript())
                .embeddingType(embeddingType)
                .build();
    }

}
