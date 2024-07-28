package com.project.otoo_java.ocr.repository;

import com.project.otoo_java.ocr.entity.OcrTalks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcrRepository extends JpaRepository<OcrTalks, Long> {
}