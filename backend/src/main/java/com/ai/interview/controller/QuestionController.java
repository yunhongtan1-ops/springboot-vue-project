package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.service.QuestionService;
import com.ai.interview.vo.QuestionVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResponse<List<QuestionVO>> listQuestions() {
        return ApiResponse.success(questionService.listQuestions());
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionVO> getQuestionById(@PathVariable Long id) {
        return ApiResponse.success(questionService.getQuestionById(id));
    }
}
