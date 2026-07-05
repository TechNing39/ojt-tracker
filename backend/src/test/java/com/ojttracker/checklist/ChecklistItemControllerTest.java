package com.ojttracker.checklist;

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
    void updateChecklistItem() throws Exception {
        String response = mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"팝콘 제조\",\"category\":\"CONCESSION\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(patch("/api/checklist-items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"팝콘/음료 제조\",\"category\":\"FLOOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("팝콘/음료 제조"))
                .andExpect(jsonPath("$.category").value("FLOOR"));
    }

    @Test
    void updateNonexistentChecklistItemReturns404() throws Exception {
        mockMvc.perform(patch("/api/checklist-items/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"제목\",\"category\":\"FLOOR\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reorderChecklistItemsWithinCategory() throws Exception {
        Long first = createItem("첫번째", "TICKETING");
        Long second = createItem("두번째", "TICKETING");
        Long third = createItem("세번째", "TICKETING");

        mockMvc.perform(patch("/api/checklist-items/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"category\":\"TICKETING\",\"orderedIds\":[" + third + "," + first + "," + second
                                        + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(third))
                .andExpect(jsonPath("$[1].id").value(first))
                .andExpect(jsonPath("$[2].id").value(second));
    }

    @Test
    void reorderWithMismatchedIdsReturns400() throws Exception {
        createItem("첫번째", "CLOSING");
        createItem("두번째", "CLOSING");

        mockMvc.perform(patch("/api/checklist-items/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"CLOSING\",\"orderedIds\":[999999]}"))
                .andExpect(status().isBadRequest());
    }

    private Long createItem(String title, String category) throws Exception {
        String response = mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"category\":\"" + category + "\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.parse(response).read("$.id", Long.class);
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
