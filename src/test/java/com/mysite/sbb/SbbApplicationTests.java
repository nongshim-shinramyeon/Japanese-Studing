package com.mysite.sbb;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mysite.sbb.domain.word.entity.Word;
import com.mysite.sbb.domain.word.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SbbApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WordRepository wordRepository;

    @Test
    void signupCreatesSessionAndReturnsNormalizedUsername() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "  TestUser  ",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void wordsEndpointSupportsLevelFilterAndPagination() throws Exception {
        mockMvc.perform(get("/api/words")
                        .param("jlptLevel", "N5")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThan(10)))
                .andExpect(jsonPath("$.data.content[*].jlptLevel", everyItem(org.hamcrest.Matchers.is("N5"))));
    }

    @Test
    void authenticatedUserCanUpdateStatusAndFilterWordsByOwnStatus() throws Exception {
        long totalWords = wordRepository.count();
        Word firstWord = wordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).getFirst();

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "learner01",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);

        mockMvc.perform(patch("/api/words/{id}/status", firstWord.getId())
                        .session(session)
                        .contentType("application/json")
                        .content("""
                                {
                                  "studyStatus": "MASTERED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(firstWord.getId()))
                .andExpect(jsonPath("$.data.studyStatus").value("MASTERED"));

        mockMvc.perform(get("/api/words")
                        .session(session)
                        .param("studyStatus", "MASTERED")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[*].id", hasItem(firstWord.getId().intValue())))
                .andExpect(jsonPath("$.data.content[*].studyStatus", everyItem(org.hamcrest.Matchers.is("MASTERED"))));

        mockMvc.perform(get("/api/words")
                        .session(session)
                        .param("studyStatus", "NEW")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value((int) totalWords - 1))
                .andExpect(jsonPath("$.data.content[*].studyStatus", everyItem(org.hamcrest.Matchers.is("NEW"))));
    }

    @Test
    void updatingWordStatusWithoutLoginReturnsUnauthorized() throws Exception {
        Word firstWord = wordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).getFirst();

        mockMvc.perform(patch("/api/words/{id}/status", firstWord.getId())
                        .contentType("application/json")
                        .content("""
                                {
                                  "studyStatus": "MASTERED"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
