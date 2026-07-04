package com.ojttracker.checklist;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ChecklistItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndListChecklistItem() throws Exception {
        mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"포스 사용법\",\"category\":\"TICKETING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("포스 사용법"))
                .andExpect(jsonPath("$.category").value("TICKETING"));

        mockMvc.perform(get("/api/checklist-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("포스 사용법"));
    }

    @Test
    void createWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"category\":\"FLOOR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithoutCategoryReturns400() throws Exception {
        mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"제목\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteChecklistItem() throws Exception {
        String response = mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"매점 운영\",\"category\":\"CONCESSION\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(delete("/api/checklist-items/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/checklist-items/" + id)).andExpect(status().isNotFound());
    }
}
