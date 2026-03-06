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

    public PracticeService(
            PracticeRecordMapper practiceRecordMapper,
            UserMapper userMapper,
            QuestionMapper questionMapper
    ) {
        this.practiceRecordMapper = practiceRecordMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
    }

    public PracticeRecordVO submitAnswer(PracticeSubmitRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getQuestionId() == null) {
            throw new IllegalArgumentException("Question id cannot be empty");
        }
        if (request.getAnswerContent() == null || request.getAnswerContent().isBlank()) {
            throw new IllegalArgumentException("Answer content cannot be empty");
        }

        String username = request.getUsername().trim();
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Question question = questionMapper.findById(request.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }

        PracticeRecord practiceRecord = new PracticeRecord();
        practiceRecord.setUserId(user.getId());
        practiceRecord.setQuestionId(question.getId());
        practiceRecord.setAnswerContent(request.getAnswerContent().trim());
        practiceRecordMapper.save(practiceRecord);

        return practiceRecordMapper.findById(practiceRecord.getId());
    }

    public List<PracticeRecordVO> getHistoryByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        User user = userMapper.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return practiceRecordMapper.findByUsername(username.trim());
    }
}