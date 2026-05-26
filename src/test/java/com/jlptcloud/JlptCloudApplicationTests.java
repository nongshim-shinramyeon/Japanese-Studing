package com.jlptcloud;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jlptcloud.domain.word.entity.Word;
import com.jlptcloud.domain.word.repository.WordRepository;
import com.jayway.jsonpath.JsonPath;
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
public class JlptCloudApplicationTests {

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

    @Test
    void creatingWordWithoutLoginReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/words")
                        .contentType("application/json")
                        .content("""
                                {
                                  "japanese": "雲",
                                  "reading": "くも",
                                  "meaning": "cloud",
                                  "partOfSpeech": "noun",
                                  "exampleSentence": "雲が多いです。",
                                  "jlptLevel": "N5",
                                  "studyStatus": "NEW"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void reviewAnswerSchedulesNextReviewAndUpdatesDashboard() throws Exception {
        Word firstWord = wordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).getFirst();

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "reviewer01",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);

        mockMvc.perform(patch("/api/words/{id}/review", firstWord.getId())
                        .session(session)
                        .contentType("application/json")
                        .content("""
                                {
                                  "correct": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studyStatus").value("LEARNING"))
                .andExpect(jsonPath("$.data.correctStreak").value(1))
                .andExpect(jsonPath("$.data.reviewCount").value(1))
                .andExpect(jsonPath("$.data.nextReviewAt").isNotEmpty());

        mockMvc.perform(get("/api/words/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalWords").value(greaterThan(10)))
                .andExpect(jsonPath("$.data.levels.length()").value(5));
    }

    @Test
    void markingWordStudiedAddsItToReviewQueue() throws Exception {
        Word firstWord = wordRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).getFirst();

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "studied01",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);

        mockMvc.perform(patch("/api/words/{id}/study", firstWord.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studied").value(true))
                .andExpect(jsonPath("$.data.memoryStage").value(1))
                .andExpect(jsonPath("$.data.memoryScore").value(100.0))
                .andExpect(jsonPath("$.data.currentMemoryScore").value(100.0))
                .andExpect(jsonPath("$.data.nextReviewAt").isNotEmpty());

        mockMvc.perform(get("/api/words/review")
                        .session(session)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[*].id", hasItem(firstWord.getId().intValue())));
    }

    @Test
    void reviewQueuePrioritizesLowerMemoryScore() throws Exception {
        java.util.List<Word> words = wordRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Word lowScoreWord = words.get(0);
        Word highScoreWord = words.get(1);

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "reviewrank01",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);

        mockMvc.perform(patch("/api/words/{id}/study", lowScoreWord.getId()).session(session))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/words/{id}/study", highScoreWord.getId()).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/words/{id}/review", lowScoreWord.getId())
                        .session(session)
                        .contentType("application/json")
                        .content("""
                                {
                                  "correct": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentMemoryScore").value(70.0));

        mockMvc.perform(get("/api/words/review")
                        .session(session)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(lowScoreWord.getId().intValue()))
                .andExpect(jsonPath("$.data.content[1].id").value(highScoreWord.getId().intValue()));
    }

    @Test
    void onlyCommentOwnerCanDeleteComment() throws Exception {
        MvcResult ownerSignup = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "comment-owner",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession ownerSession = (MockHttpSession) ownerSignup.getRequest().getSession(false);

        MvcResult postResult = mockMvc.perform(post("/api/community/posts")
                        .session(ownerSession)
                        .contentType("application/json")
                        .content("""
                                {
                                  "authorName": "Owner",
                                  "title": "N2 reading tips",
                                  "content": "How do you review long readings?"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Integer postId = JsonPath.read(postResult.getResponse().getContentAsString(), "$.data.id");

        MvcResult commentResult = mockMvc.perform(post("/api/community/posts/{postId}/comments", postId)
                        .session(ownerSession)
                        .contentType("application/json")
                        .content("""
                                {
                                  "parentId": null,
                                  "authorName": "Owner",
                                  "content": "I split the text by paragraph."
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Integer commentId = JsonPath.read(commentResult.getResponse().getContentAsString(), "$.data.id");

        MvcResult otherSignup = mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "other-user",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession otherSession = (MockHttpSession) otherSignup.getRequest().getSession(false);

        mockMvc.perform(delete("/api/community/comments/{commentId}", commentId)
                        .session(otherSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMUNITY_COMMENT_FORBIDDEN"));
    }
}
