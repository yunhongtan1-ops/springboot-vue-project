package com.ai.interview.service;

import com.ai.interview.entity.Question;
import com.ai.interview.vo.PracticeRecordVO;

public interface PracticeReviewGenerator {

    GeneratedPracticeReview generate(PracticeRecordVO practiceRecord, Question question);
}