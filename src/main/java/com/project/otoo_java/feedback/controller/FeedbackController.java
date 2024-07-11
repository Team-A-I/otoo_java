package com.project.otoo_java.feedback.controller;

import com.project.otoo_java.feedback.model.dto.FeedbackDto;
import com.project.otoo_java.feedback.model.entity.Feedback;
import com.project.otoo_java.feedback.model.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public void feedback(@RequestBody Feedback feedback) {
        feedbackService.insertFeedback(feedback);
    }

}
