package com.project.otoo_java.chatbot.model.repository;

import com.project.otoo_java.chatbot.model.entity.EmotionReports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionReportsRepository extends JpaRepository<EmotionReports, Long> {
    List<EmotionReports> findAllByUsersCode(String usersCode);
}
