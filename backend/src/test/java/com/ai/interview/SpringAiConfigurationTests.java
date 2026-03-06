package com.ai.interview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.ai.model.chat=openai",
        "spring.ai.openai.api-key=test-key",
        "spring.ai.openai.chat.api-key=test-key",
        "spring.ai.openai.base-url=https://api.deepseek.com",
        "spring.ai.openai.chat.base-url=https://api.deepseek.com",
        "spring.ai.openai.chat.options.model=deepseek-chat"
})
@AutoConfigureMockMvc
class SpringAiConfigurationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openAiCompatibleConfigurationEnablesRemoteReviewGenerator() throws Exception {
        mockMvc.perform(get("/api/health/ai-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.chatProvider").value("openai"))
                .andExpect(jsonPath("$.data.chatModel").value("deepseek-chat"))
                .andExpect(jsonPath("$.data.chatBaseUrl").value("https://api.deepseek.com"))
                .andExpect(jsonPath("$.data.apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.data.chatModelAvailable").value(true))
                .andExpect(jsonPath("$.data.reviewGeneratorBean").value("SpringAiPracticeReviewGenerator"))
                .andExpect(jsonPath("$.data.remoteReviewEnabled").value(true));
    }
}