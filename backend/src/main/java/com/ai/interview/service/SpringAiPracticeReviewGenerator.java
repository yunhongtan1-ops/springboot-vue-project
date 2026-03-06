package com.ai.interview.service;

import com.ai.interview.entity.Question;
import com.ai.interview.vo.PracticeRecordVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
public class SpringAiPracticeReviewGenerator implements PracticeReviewGenerator {

    private final ChatClient chatClient;

    public SpringAiPracticeReviewGenerator(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @Override
    public GeneratedPracticeReview generate(PracticeRecordVO practiceRecord, Question question) {
        AiPracticeReviewResponse response = chatClient.prompt()
                .system(systemPrompt())
                .user(user -> user.text(userPrompt())
                        .param("questionTitle", question.getTitle())
                        .param("questionContent", question.getContent())
                        .param("questionType", question.getType())
                        .param("questionDifficulty", question.getDifficulty())
                        .param("answerContent", practiceRecord.getAnswerContent()))
                .call()
                .entity(AiPracticeReviewResponse.class);

        return new GeneratedPracticeReview(
                normalizeScore(response.getOverallScore()),
                fallbackText(response.getSummary(), "点评结果已生成，但总结内容为空。"),
                fallbackText(response.getHighlightExcerpt(), trimAnswer(practiceRecord.getAnswerContent())),
                normalizeList(response.getStrengths(), "当前回答已经具备可继续打磨的基础。"),
                normalizeList(response.getImprovements(), "建议补充复杂度分析，并更明确地说明方案取舍。"),
                normalizeList(response.getFollowUps(), "如果面试官继续追问更优方案，你会优先补充什么？"),
                "SPRING_AI_OPENAI"
        );
    }

    private String systemPrompt() {
        return "你是一名资深技术面试官。请审阅候选人的回答，并且只返回严格的 JSON 对象。" +
                "JSON 必须包含这些字段：overallScore（1 到 10 的整数）、summary（字符串）、highlightExcerpt（字符串）、" +
                "strengths（2 到 4 条字符串数组）、improvements（2 到 4 条字符串数组）、followUps（2 到 4 条字符串数组）。" +
                "点评内容要务实、贴近技术面试场景、表达简洁，所有值都使用简体中文。";
    }

    private String userPrompt() {
        return "题目标题：{questionTitle}\n" +
                "题目详情：{questionContent}\n" +
                "题目类型：{questionType}\n" +
                "题目难度：{questionDifficulty}\n" +
                "候选人回答：\n{answerContent}\n\n" +
                "请只返回 JSON 对象，不要使用 Markdown 包裹。";
    }

    private int normalizeScore(Integer score) {
        if (score == null) {
            return 6;
        }
        return Math.max(1, Math.min(score, 10));
    }

    private String fallbackText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private List<String> normalizeList(List<String> values, String fallback) {
        if (values == null || values.isEmpty()) {
            return List.of(fallback);
        }

        List<String> normalized = values.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .toList();

        return normalized.isEmpty() ? List.of(fallback) : normalized;
    }

    private String trimAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return "未提供答案内容。";
        }

        String normalized = answer.trim();
        return normalized.length() > 160 ? normalized.substring(0, 160) + "..." : normalized;
    }
}