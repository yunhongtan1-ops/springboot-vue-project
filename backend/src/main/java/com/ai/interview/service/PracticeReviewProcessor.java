package com.ai.interview.service;

import com.ai.interview.entity.PracticeReview;
import com.ai.interview.entity.Question;
import com.ai.interview.mapper.PracticeRecordMapper;
import com.ai.interview.mapper.PracticeReviewMapper;
import com.ai.interview.mapper.QuestionMapper;
import com.ai.interview.vo.PracticeRecordVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PracticeReviewProcessor {

    private final PracticeReviewMapper practiceReviewMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final QuestionMapper questionMapper;
    private final PracticeReviewGenerator practiceReviewGenerator;
    private final ObjectMapper objectMapper;

    public PracticeReviewProcessor(
            PracticeReviewMapper practiceReviewMapper,
            PracticeRecordMapper practiceRecordMapper,
            QuestionMapper questionMapper,
            PracticeReviewGenerator practiceReviewGenerator,
            ObjectMapper objectMapper
    ) {
        this.practiceReviewMapper = practiceReviewMapper;
        this.practiceRecordMapper = practiceRecordMapper;
        this.questionMapper = questionMapper;
        this.practiceReviewGenerator = practiceReviewGenerator;
        this.objectMapper = objectMapper;
    }

    @Async
    public void generateReviewAsync(Long practiceRecordId) {
        PracticeReview existingReview = practiceReviewMapper.findByPracticeRecordId(practiceRecordId);
        if (existingReview != null && existingReview.getStatus() != null && existingReview.getStatus().name().equals("SUCCESS")) {
            return;
        }

        try {
            PracticeRecordVO practiceRecord = practiceRecordMapper.findById(practiceRecordId);
            if (practiceRecord == null) {
                practiceReviewMapper.updateFailureByPracticeRecordId(practiceRecordId, "练习记录不存在");
                return;
            }

            Question question = questionMapper.findById(practiceRecord.getQuestionId());
            if (question == null) {
                practiceReviewMapper.updateFailureByPracticeRecordId(practiceRecordId, "生成点评时未找到题目");
                return;
            }

            GeneratedPracticeReview generatedReview = practiceReviewGenerator.generate(practiceRecord, question);
            practiceReviewMapper.updateSuccessByPracticeRecordId(
                    practiceRecordId,
                    generatedReview.overallScore(),
                    generatedReview.summary(),
                    generatedReview.highlightExcerpt(),
                    objectMapper.writeValueAsString(generatedReview.strengths()),
                    objectMapper.writeValueAsString(generatedReview.improvements()),
                    objectMapper.writeValueAsString(generatedReview.followUps()),
                    generatedReview.generatorName()
            );
        } catch (Exception ex) {
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "点评生成失败"
                    : ex.getMessage();
            practiceReviewMapper.updateFailureByPracticeRecordId(practiceRecordId, message);
        }
    }
}