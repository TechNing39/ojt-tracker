package com.ojttracker.trainee;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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

    @Test
    void createListAndDeleteTrainee() throws Exception {
        String response = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"철수\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("철수"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/trainees")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/trainees/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/trainees/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNote() throws Exception {
        String response = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"영희\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(patch("/api/trainees/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"적응 빠름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("적응 빠름"));
    }

    @Test
    void updateNoteForNonexistentTraineeReturns404() throws Exception {
        mockMvc.perform(patch("/api/trainees/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
