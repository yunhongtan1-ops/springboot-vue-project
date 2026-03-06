package com.ai.interview.controller;

import com.ai.interview.common.ApiResponse;
import com.ai.interview.service.PracticeReviewGenerator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final Environment environment;
    private final PracticeReviewGenerator practiceReviewGenerator;
    private final ListableBeanFactory beanFactory;

    public HealthController(
            Environment environment,
            PracticeReviewGenerator practiceReviewGenerator,
            ListableBeanFactory beanFactory
    ) {
        this.environment = environment;
        this.practiceReviewGenerator = practiceReviewGenerator;
        this.beanFactory = beanFactory;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "backend");
        return ApiResponse.success(data);
    }

    @GetMapping("/health/ai-config")
    public ApiResponse<Map<String, Object>> aiConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("chatProvider", environment.getProperty("spring.ai.model.chat", "none"));
        data.put("chatModel", environment.getProperty("spring.ai.openai.chat.options.model", ""));
        data.put(
                "chatBaseUrl",
                environment.getProperty(
                        "spring.ai.openai.chat.base-url",
                        environment.getProperty("spring.ai.openai.base-url", "")
                )
        );
        data.put("chatTemperature", environment.getProperty("spring.ai.openai.chat.options.temperature", ""));
        data.put("apiKeyConfigured", hasText(environment.getProperty("spring.ai.openai.chat.api-key"))
                || hasText(environment.getProperty("spring.ai.openai.api-key")));
        data.put("chatModelAvailable", beanFactory.getBeanNamesForType(ChatModel.class, false, false).length > 0);
        data.put("chatClientBuilderAvailable", beanFactory.getBeanNamesForType(ChatClient.Builder.class, false, false).length > 0);
        data.put("reviewGeneratorBean", practiceReviewGenerator.getClass().getSimpleName());
        data.put("reviewGeneratorClass", practiceReviewGenerator.getClass().getName());
        data.put(
                "remoteReviewEnabled",
                "SpringAiPracticeReviewGenerator".equals(practiceReviewGenerator.getClass().getSimpleName())
        );
        return ApiResponse.success(data);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}