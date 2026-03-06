package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.dto.PracticeSubmitRequest;
import com.ai.interview.service.PracticeService;
import com.ai.interview.vo.PracticeRecordVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @PostMapping("/submit")
    public ApiResponse<PracticeRecordVO> submitAnswer(@RequestBody PracticeSubmitRequest request) {
        return ApiResponse.success(practiceService.submitAnswer(request));
    }

    @GetMapping("/history")
    public ApiResponse<List<PracticeRecordVO>> getHistory(@RequestParam String username) {
        return ApiResponse.success(practiceService.getHistoryByUsername(username));
    }
}