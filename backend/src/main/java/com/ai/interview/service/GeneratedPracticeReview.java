package com.ai.interview.service;

import java.util.List;

public record GeneratedPracticeReview(
        int overallScore,
        String summary,
        String highlightExcerpt,
        List<String> strengths,
        List<String> improvements,
        List<String> followUps,
        String generatorName
) {
}