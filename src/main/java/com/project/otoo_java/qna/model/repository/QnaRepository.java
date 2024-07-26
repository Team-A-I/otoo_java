package com.project.otoo_java.qna.model.repository;

import com.project.otoo_java.qna.model.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  QnaRepository extends JpaRepository<Qna, String> {
}
