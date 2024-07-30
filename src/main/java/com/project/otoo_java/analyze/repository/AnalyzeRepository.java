package com.project.otoo_java.analyze.repository;

import com.project.otoo_java.analyze.entity.Talks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalyzeRepository extends JpaRepository<Talks, Long>{
    List<Talks> findAllByUsersCode(String usersCode);
}

