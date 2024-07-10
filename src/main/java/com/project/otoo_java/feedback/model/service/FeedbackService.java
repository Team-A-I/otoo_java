package com.project.otoo_java.feedback.model.service;

import com.project.otoo_java.feedback.model.entity.Feedback;
import com.project.otoo_java.feedback.model.repository.FeedbackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public void insertFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }
}
