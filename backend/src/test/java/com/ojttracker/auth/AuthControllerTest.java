package com.ojttracker.auth;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void siteVerifyWithCorrectPinReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/site/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"JUNGGYE\",\"pin\":\"test-junggye-pin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SITE"))
                .andExpect(jsonPath("$.siteName").value("중계"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void siteVerifyWithWrongPinReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/site/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"JUNGGYE\",\"pin\":\"wrong-pin\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void siteVerifyWithUnknownCodeReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/site/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"UNKNOWN\",\"pin\":\"test-junggye-pin\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminVerifyWithCorrectPinReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/admin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"test-admin-pin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.sites", hasSize(5)));
    }

    @Test
    void adminVerifyWithWrongPinReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/admin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"wrong-pin\"}"))
                .andExpect(status().isUnauthorized());
    }
}
