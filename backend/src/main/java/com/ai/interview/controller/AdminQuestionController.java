package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.dto.QuestionSaveRequest;
import com.ai.interview.service.QuestionService;
import com.ai.interview.vo.QuestionVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionService questionService;

    public AdminQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ApiResponse<QuestionVO> createQuestion(@RequestBody QuestionSaveRequest request) {
        return ApiResponse.success(questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionVO> updateQuestion(@PathVariable Long id, @RequestBody QuestionSaveRequest request) {
        return ApiResponse.success(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.success("题目已删除", null);
    }
}