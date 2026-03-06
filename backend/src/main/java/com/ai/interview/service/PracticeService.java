package com.ai.interview.service;

import com.ai.interview.dto.PracticeSubmitRequest;
import com.ai.interview.entity.PracticeRecord;
import com.ai.interview.entity.Question;
import com.ai.interview.entity.User;
import com.ai.interview.mapper.PracticeRecordMapper;
import com.ai.interview.mapper.QuestionMapper;
import com.ai.interview.mapper.UserMapper;
import com.ai.interview.vo.PracticeRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PracticeService {

    private final PracticeRecordMapper practiceRecordMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final PracticeReviewService practiceReviewService;

    public PracticeService(
            PracticeRecordMapper practiceRecordMapper,
            UserMapper userMapper,
            QuestionMapper questionMapper,
            PracticeReviewService practiceReviewService
    ) {
        this.practiceRecordMapper = practiceRecordMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.practiceReviewService = practiceReviewService;
    }

    public PracticeRecordVO submitAnswer(String username, PracticeSubmitRequest request) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getQuestionId() == null) {
            throw new IllegalArgumentException("题目编号不能为空");
        }
        if (request.getAnswerContent() == null || request.getAnswerContent().isBlank()) {
            throw new IllegalArgumentException("答案内容不能为空");
        }

        String normalizedUsername = username.trim();
        User user = userMapper.findByUsername(normalizedUsername);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        Question question = questionMapper.findById(request.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        PracticeRecord practiceRecord = new PracticeRecord();
        practiceRecord.setUserId(user.getId());
        practiceRecord.setQuestionId(question.getId());
        practiceRecord.setAnswerContent(request.getAnswerContent().trim());
        practiceRecordMapper.save(practiceRecord);

        PracticeRecordVO savedRecord = practiceRecordMapper.findById(practiceRecord.getId());
        practiceReviewService.requestReview(savedRecord.getId());
        return savedRecord;
    }

    public List<PracticeRecordVO> getHistoryByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        User user = userMapper.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        return practiceRecordMapper.findByUserId(user.getId());
    }
}