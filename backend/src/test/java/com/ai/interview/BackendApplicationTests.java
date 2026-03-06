package com.ai.interview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullApiFlowWorks() throws Exception {
        String username = "test_" + System.currentTimeMillis();
        String password = "123456";

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));

        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", username);
        registerRequest.put("password", password);
        registerRequest.put("nickname", "tester");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username));

        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value(username))
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(1));

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("username", username);
        submitRequest.put("questionId", 1);
        submitRequest.put("answerContent", "I would solve it with a hash map.");

        mockMvc.perform(post("/api/practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.questionId").value(1));

        mockMvc.perform(get("/api/practice/history").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].username").value(username))
                .andExpect(jsonPath("$.data[0].questionId").value(1));
    }

    @Test
    void adminQuestionCrudWorks() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());

        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("title", "Admin Question " + suffix);
        createRequest.put("content", "Describe your favorite JVM troubleshooting workflow.");
        createRequest.put("type", "INTERVIEW");
        createRequest.put("difficulty", "MEDIUM");

        MvcResult createResult = mockMvc.perform(post("/api/admin/questions")
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

        mockMvc.perform(delete("/api/admin/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Question deleted"));

        mockMvc.perform(get("/api/questions/{id}", questionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Question not found"));
    }
}