package com.ojttracker.trainee;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.ojttracker.auth.Role;
import com.ojttracker.auth.SiteCode;
import com.ojttracker.auth.SiteRepository;
import com.ojttracker.auth.TokenService;
import org.junit.jupiter.api.BeforeEach;
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
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SiteRepository siteRepository;

    private Long junggyeId;

    @BeforeEach
    void setUp() {
        junggyeId = siteRepository.findByCode(SiteCode.JUNGGYE.name()).orElseThrow().getId();
    }

    private String adminAuth() {
        return "Bearer " + tokenService.issueToken(Role.ADMIN, null);
    }

    @Test
    void createListAndDeleteTrainee() throws Exception {
        String response = mockMvc.perform(post("/api/trainees?siteId=" + junggyeId)
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"철수\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("철수"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/trainees?siteId=" + junggyeId).header("Authorization", adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/trainees/" + id).header("Authorization", adminAuth()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/trainees/" + id).header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/trainees?siteId=" + junggyeId)
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNote() throws Exception {
        String response = mockMvc.perform(post("/api/trainees?siteId=" + junggyeId)
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"영희\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(patch("/api/trainees/" + id)
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"적응 빠름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("적응 빠름"));
    }

    @Test
    void updateNoteForNonexistentTraineeReturns404() throws Exception {
        mockMvc.perform(patch("/api/trainees/9999")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/trainees?siteId=" + junggyeId)).andExpect(status().isUnauthorized());
    }

    @Test
    void listWithOtherSiteTokenReturns403() throws Exception {
        Long sangbongId =
                siteRepository.findByCode(SiteCode.SANGBONG.name()).orElseThrow().getId();
        String otherSiteAuth = "Bearer " + tokenService.issueToken(Role.SITE, sangbongId);

        mockMvc.perform(get("/api/trainees?siteId=" + junggyeId).header("Authorization", otherSiteAuth))
                .andExpect(status().isForbidden());
    }
}
