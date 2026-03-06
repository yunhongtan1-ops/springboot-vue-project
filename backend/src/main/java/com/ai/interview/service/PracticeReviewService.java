package com.ai.interview.service;

import com.ai.interview.entity.PracticeReview;
import com.ai.interview.entity.ReviewStatus;
import com.ai.interview.mapper.PracticeRecordMapper;
import com.ai.interview.mapper.PracticeReviewMapper;
import com.ai.interview.vo.PracticeRecordVO;
import com.ai.interview.vo.PracticeReviewVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PracticeReviewService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final PracticeRecordMapper practiceRecordMapper;
    private final PracticeReviewMapper practiceReviewMapper;
    private final PracticeReviewProcessor practiceReviewProcessor;
    private final ObjectMapper objectMapper;

    public PracticeReviewService(
            PracticeRecordMapper practiceRecordMapper,
            PracticeReviewMapper practiceReviewMapper,
            PracticeReviewProcessor practiceReviewProcessor,
            ObjectMapper objectMapper
    ) {
        this.practiceRecordMapper = practiceRecordMapper;
        this.practiceReviewMapper = practiceReviewMapper;
        this.practiceReviewProcessor = practiceReviewProcessor;
        this.objectMapper = objectMapper;
    }

    public void requestReview(Long practiceRecordId) {
        if (practiceRecordId == null) {
            throw new IllegalArgumentException("练习记录编号不能为空");
        }

        practiceReviewMapper.insertPending(practiceRecordId);
        practiceReviewProcessor.generateReviewAsync(practiceRecordId);
    }

    public PracticeReviewVO getReviewByRecordId(Long practiceRecordId, String username) {
        if (practiceRecordId == null) {
            throw new IllegalArgumentException("练习记录编号不能为空");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        PracticeRecordVO practiceRecord = practiceRecordMapper.findById(practiceRecordId);
        if (practiceRecord == null || !username.trim().equals(practiceRecord.getUsername())) {
            throw new IllegalArgumentException("练习记录不存在");
        }

        PracticeReview review = practiceReviewMapper.findByPracticeRecordId(practiceRecordId);
        if (review == null) {
            practiceReviewMapper.insertPending(practiceRecordId);
            practiceReviewProcessor.generateReviewAsync(practiceRecordId);
            review = practiceReviewMapper.findByPracticeRecordId(practiceRecordId);
        } else if (review.getStatus() == ReviewStatus.PENDING) {
            practiceReviewProcessor.generateReviewAsync(practiceRecordId);
        }

        if (review == null) {
            PracticeReviewVO pending = new PracticeReviewVO();
            pending.setPracticeRecordId(practiceRecordId);
            pending.setStatus(ReviewStatus.PENDING.name());
            pending.setStrengths(List.of());
            pending.setImprovements(List.of());
            pending.setFollowUps(List.of());
            return pending;
        }

        return toPracticeReviewVO(review);
    }

    private PracticeReviewVO toPracticeReviewVO(PracticeReview review) {
        PracticeReviewVO result = new PracticeReviewVO();
        result.setId(review.getId());
        result.setPracticeRecordId(review.getPracticeRecordId());
        result.setStatus(review.getStatus() == null ? ReviewStatus.PENDING.name() : review.getStatus().name());
        result.setOverallScore(review.getOverallScore());
        result.setSummary(review.getSummary());
        result.setHighlightExcerpt(review.getHighlightExcerpt());
        result.setStrengths(readStringList(review.getStrengthsJson()));
        result.setImprovements(readStringList(review.getImprovementsJson()));
        result.setFollowUps(readStringList(review.getFollowUpsJson()));
        result.setGeneratorName(review.getGeneratorName());
        result.setErrorMessage(review.getErrorMessage());
        result.setCreatedAt(review.getCreatedAt());
        result.setUpdatedAt(review.getUpdatedAt());
        return result;
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception ex) {
            return List.of();
        }
    }
}