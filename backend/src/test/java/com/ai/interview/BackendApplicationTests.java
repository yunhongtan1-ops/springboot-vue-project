package com.ai.interview;

import com.ai.interview.entity.User;
import com.ai.interview.entity.UserRole;
import com.ai.interview.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void fullApiFlowWorks() throws Exception {
        String username = "test_" + System.currentTimeMillis();
        String password = "123456";

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/api/health/ai-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.chatProvider").value("none"))
                .andExpect(jsonPath("$.data.apiKeyConfigured").value(false))
                .andExpect(jsonPath("$.data.reviewGeneratorBean").value("HeuristicPracticeReviewGenerator"));

        registerUser(username, password, "tester")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.role").value("USER"));

        Map<String, Object> invalidLoginRequest = new HashMap<>();
        invalidLoginRequest.put("username", username);
        invalidLoginRequest.put("password", password + "_wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        String authHeader = bearer(loginAndGetToken(username, password, "USER"));

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.role").value("USER"));

        mockMvc.perform(get("/api/practice/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").exists());

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("questionId", 1);
        submitRequest.put("answerContent", "I would solve it with a hash map. First I store visited values, then I return indices. Time complexity is O(n) and I would also check empty input.");

        MvcResult submitResult = mockMvc.perform(post("/api/practice/submit")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.questionId").value(1))
                .andReturn();

        JsonNode submitJson = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        long recordId = submitJson.path("data").path("id").asLong();

        mockMvc.perform(get("/api/practice/history")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].username").value(username))
                .andExpect(jsonPath("$.data[0].questionId").value(1));

        JsonNode reviewJson = waitForPracticeReview(authHeader, recordId);
        Assertions.assertEquals("SUCCESS", reviewJson.path("status").asText());
        Assertions.assertTrue(reviewJson.path("overallScore").asInt() >= 1);
        Assertions.assertFalse(reviewJson.path("summary").asText().isBlank());
        Assertions.assertTrue(reviewJson.path("strengths").isArray());
        Assertions.assertTrue(reviewJson.path("strengths").size() > 0);
    }

    @Test
    void adminQuestionCrudWorks() throws Exception {
        String username = "admin_test_" + System.currentTimeMillis();
        String password = "123456";
        String suffix = String.valueOf(System.currentTimeMillis());

        registerUser(username, password, "admin")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String userAuthHeader = bearer(loginAndGetToken(username, password, "USER"));

        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("title", "Admin Question " + suffix);
        createRequest.put("content", "Describe your favorite JVM troubleshooting workflow.");
        createRequest.put("type", "INTERVIEW");
        createRequest.put("difficulty", "MEDIUM");

        mockMvc.perform(post("/api/admin/questions")
                        .header(HttpHeaders.AUTHORIZATION, userAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        User user = userMapper.findByUsername(username);
        userMapper.updateRoleById(user.getId(), UserRole.ADMIN.name());

        String adminAuthHeader = bearer(loginAndGetToken(username, password, "ADMIN"));

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        MvcResult createResult = mockMvc.perform(post("/api/admin/questions")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Admin Question " + suffix))
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long questionId = createJson.path("data").path("id").asLong();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Admin Question " + suffix);
        updateRequest.put("content", "Explain how you would diagnose a slow SQL query.");
        updateRequest.put("type", "INTERVIEW");
        updateRequest.put("difficulty", "HARD");

        mockMvc.perform(put("/api/admin/questions/{id}", questionId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(questionId))
                .andExpect(jsonPath("$.data.title").value("Updated Admin Question " + suffix))
                .andExpect(jsonPath("$.data.difficulty").value("HARD"));

        mockMvc.perform(get("/api/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(questionId))
                .andExpect(jsonPath("$.data.title").value("Updated Admin Question " + suffix));

        mockMvc.perform(delete("/api/admin/questions/{id}", questionId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("题目已删除"));

        mockMvc.perform(get("/api/questions/{id}", questionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("题目不存在"));
    }

    private ResultActions registerUser(String username, String password, String nickname) throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", username);
        registerRequest.put("password", password);
        registerRequest.put("nickname", nickname);

        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));
    }

    private String loginAndGetToken(String username, String password, String expectedRole) throws Exception {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value(username))
                .andExpect(jsonPath("$.data.user.role").value(expectedRole))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return loginJson.path("data").path("token").asText();
    }

    private JsonNode waitForPracticeReview(String authHeader, long recordId) throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            MvcResult result = mockMvc.perform(get("/api/practice/review/{recordId}", recordId)
                            .header(HttpHeaders.AUTHORIZATION, authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andReturn();

            JsonNode reviewJson = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
            String status = reviewJson.path("status").asText();
            if (!"PENDING".equals(status)) {
                return reviewJson;
            }

            Thread.sleep(150);
        }

        Assertions.fail("Practice review generation timed out");
        return objectMapper.createObjectNode();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}