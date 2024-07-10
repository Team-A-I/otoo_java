package com.project.otoo_java.feedback.model.repository;

import com.project.otoo_java.feedback.model.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
