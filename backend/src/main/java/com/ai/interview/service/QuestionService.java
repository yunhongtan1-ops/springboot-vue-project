package com.ai.interview.service;

import com.ai.interview.dto.QuestionSaveRequest;
import com.ai.interview.entity.Question;
import com.ai.interview.mapper.QuestionMapper;
import com.ai.interview.vo.QuestionVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<QuestionVO> listQuestions() {
        List<Question> questions = questionMapper.findAll();
        List<QuestionVO> result = new ArrayList<>();

        for (Question question : questions) {
            result.add(toQuestionVO(question));
        }
        return result;
    }

    public QuestionVO getQuestionById(Long id) {
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        return toQuestionVO(question);
    }

    public QuestionVO createQuestion(QuestionSaveRequest request) {
        Question question = buildQuestionFromRequest(null, request);
        questionMapper.save(question);
        return toQuestionVO(question);
    }

    public QuestionVO updateQuestion(Long id, QuestionSaveRequest request) {
        Question existingQuestion = questionMapper.findById(id);
        if (existingQuestion == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        Question question = buildQuestionFromRequest(id, request);
        questionMapper.updateById(question);
        return toQuestionVO(questionMapper.findById(id));
    }

    public void deleteQuestion(Long id) {
        Question existingQuestion = questionMapper.findById(id);
        if (existingQuestion == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        questionMapper.deleteById(id);
    }

    private Question buildQuestionFromRequest(Long id, QuestionSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("题目标题不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("题目类型不能为空");
        }
        if (request.getDifficulty() == null || request.getDifficulty().isBlank()) {
            throw new IllegalArgumentException("题目难度不能为空");
        }

        Question question = new Question();
        question.setId(id);
        question.setTitle(request.getTitle().trim());
        question.setContent(request.getContent().trim());
        question.setType(request.getType().trim());
        question.setDifficulty(request.getDifficulty().trim());
        return question;
    }

    private QuestionVO toQuestionVO(Question question) {
        return new QuestionVO(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getType(),
                question.getDifficulty()
        );
    }
}