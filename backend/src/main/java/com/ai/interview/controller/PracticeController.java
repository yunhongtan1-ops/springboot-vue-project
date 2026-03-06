package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.dto.PracticeSubmitRequest;
import com.ai.interview.security.AppUserPrincipal;
import com.ai.interview.service.PracticeReviewService;
import com.ai.interview.service.PracticeService;
import com.ai.interview.vo.PracticeRecordVO;
import com.ai.interview.vo.PracticeReviewVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;
    private final PracticeReviewService practiceReviewService;

    public PracticeController(PracticeService practiceService, PracticeReviewService practiceReviewService) {
        this.practiceService = practiceService;
        this.practiceReviewService = practiceReviewService;
    }

    @PostMapping("/submit")
    public ApiResponse<PracticeRecordVO> submitAnswer(
            @RequestBody PracticeSubmitRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(practiceService.submitAnswer(principal.getUsername(), request));
    }

    @GetMapping("/history")
    public ApiResponse<List<PracticeRecordVO>> getHistory(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.success(practiceService.getHistoryByUsername(principal.getUsername()));
    }

    @GetMapping("/review/{recordId}")
    public ApiResponse<PracticeReviewVO> getReview(
            @PathVariable Long recordId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(practiceReviewService.getReviewByRecordId(recordId, principal.getUsername()));
    }
}