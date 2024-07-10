package com.project.otoo_java.analyze.repository;

import com.project.otoo_java.analyze.entity.Talks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyzeRepository extends JpaRepository<Talks, Long>{
}

