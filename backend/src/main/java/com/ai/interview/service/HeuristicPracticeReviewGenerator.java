package com.ai.interview.service;

import com.ai.interview.entity.Question;
import com.ai.interview.vo.PracticeRecordVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HeuristicPracticeReviewGenerator implements PracticeReviewGenerator {

    @Override
    public GeneratedPracticeReview generate(PracticeRecordVO practiceRecord, Question question) {
        String answer = practiceRecord.getAnswerContent() == null ? "" : practiceRecord.getAnswerContent().trim();
        int answerLength = answer.length();
        int paragraphCount = answer.isBlank() ? 0 : answer.split("\\R+").length;
        boolean mentionsComplexity = matchesAny(
                answer,
                "O(",
                "complexity",
                "复杂度",
                "时间复杂度",
                "空间复杂度"
        );
        boolean mentionsEdgeCases = matchesAny(
                answer,
                "边界",
                "异常",
                "null",
                "corner",
                "edge",
                "空数组",
                "空链表"
        );
        boolean mentionsTradeOffs = matchesAny(
                answer,
                "trade-off",
                "取舍",
                "对比",
                "alternative",
                "优化",
                "更优"
        );
        boolean mentionsSteps = matchesAny(
                answer,
                "first",
                "then",
                "最后",
                "首先",
                "然后",
                "步骤",
                "思路"
        );

        int overallScore = 4;
        if (answerLength >= 80) {
            overallScore++;
        }
        if (answerLength >= 180) {
            overallScore++;
        }
        if (paragraphCount >= 2) {
            overallScore++;
        }
        if (mentionsComplexity) {
            overallScore++;
        }
        if (mentionsEdgeCases) {
            overallScore++;
        }
        if (mentionsTradeOffs || mentionsSteps) {
            overallScore++;
        }
        overallScore = Math.max(1, Math.min(overallScore, 10));

        String summary;
        if (overallScore >= 9) {
            summary = "这份回答已经接近真实面试中的可用水准，可以继续补足方案取舍和表达细节。";
        } else if (overallScore >= 7) {
            summary = "主要思路已经具备，建议继续加强复杂度分析和边界条件说明，让回答更有说服力。";
        } else {
            summary = "这份答案已经形成初稿，但还需要更清晰的结构和更扎实的技术细节。";
        }

        List<String> strengths = new ArrayList<>();
        strengths.add(paragraphCount >= 2
                ? "回答已经分成多个段落，面试官更容易快速跟上你的表达。"
                : "你已经给出一份完整初稿，后续可以在此基础上快速打磨。"
        );
        strengths.add(mentionsSteps
                ? "解释过程体现出明显的先后顺序，不会一下跳到结论。"
                : "答案里已经有核心思路，便于继续扩展成更完整的口头表达。"
        );
        strengths.add(mentionsComplexity
                ? "主动说明复杂度，是技术面试里很重要的加分项。"
                : "当前回答足够简洁，后续补充信息时不需要整体推翻重写。"
        );

        List<String> improvements = new ArrayList<>();
        improvements.add(mentionsComplexity
                ? "除了给出复杂度结论，还可以补充为什么这个方案会得到这样的复杂度。"
                : "请明确补充时间复杂度和空间复杂度分析。"
        );
        improvements.add(mentionsEdgeCases
                ? "可以把已经提到的边界条件展开成更具体的示例和预期行为。"
                : "建议补充空输入、重复值、空指针等边界条件。"
        );
        improvements.add(mentionsTradeOffs
                ? "可以进一步说明在真实业务场景下，为什么会选择当前方案。"
                : "建议至少补充一种替代方案，并解释它和当前方案的取舍。"
        );

        List<String> followUps = List.of(
                "如果面试官要求更优版本，你会优先优化哪一部分？",
                "如果需要在白板上两分钟讲清楚，你会怎么重新组织这份答案？",
                "针对题目《" + question.getTitle() + "》，你觉得最值得补充的生产环境边界条件是什么？"
        );

        String highlightExcerpt = answer.isBlank()
                ? "未提供答案内容。"
                : (answer.length() > 160 ? answer.substring(0, 160).trim() + "..." : answer);

        return new GeneratedPracticeReview(
                overallScore,
                summary,
                highlightExcerpt,
                strengths,
                improvements,
                followUps,
                "LOCAL_HEURISTIC_V1"
        );
    }

    private boolean matchesAny(String value, String... candidates) {
        String lower = value.toLowerCase();
        for (String candidate : candidates) {
            if (lower.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}