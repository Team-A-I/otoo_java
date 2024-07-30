package com.project.otoo_java.ocr.repository;

import com.project.otoo_java.ocr.entity.OcrTalks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OcrRepository extends JpaRepository<OcrTalks, Long> {
    List<OcrTalks> findByOcrUsersCode(String ocrUsersCode);
}