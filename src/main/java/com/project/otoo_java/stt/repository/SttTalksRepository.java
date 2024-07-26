package com.project.otoo_java.stt.repository;

import com.project.otoo_java.stt.entity.SttTalks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SttTalksRepository extends JpaRepository<SttTalks, Long> {
}