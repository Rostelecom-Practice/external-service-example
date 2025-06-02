package com.practice.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.example.dto.AddReactionRequest;
import com.practice.example.dto.CreateReviewRequest;
import com.practice.example.model.Review;
import com.practice.example.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerTest {

    private ReviewService reviewService;
    private ReviewController reviewController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reviewService = Mockito.mock(ReviewService.class);
        reviewController = new ReviewController(reviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createReview_returnsOkAndBody() throws Exception {
        // Arrange
        CreateReviewRequest req = new CreateReviewRequest();
        UUID authorId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        req.setAuthorId(authorId);
        req.setOrganizationId(orgId);
        req.setTitle("T");
        req.setContent("C");
        req.setRatingValue(5);

        Review mockReview = Review.builder()
                .id(UUID.randomUUID())
                .authorId(authorId)
                .organizationId(orgId)
                .title("T")
                .content("C")
                .publishedAt(Instant.now())
                .ratingValue(5)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewService.createReview(
                eq(authorId), eq(orgId), eq("T"), eq("C"), isNull(), eq(5)
        )).thenReturn(mockReview);

        // Act & Assert
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockReview.getId().toString()))
                .andExpect(jsonPath("$.title").value("T"));

        verify(reviewService, times(1))
                .createReview(eq(authorId), eq(orgId), eq("T"), eq("C"), isNull(), eq(5));
    }

    @Test
    void replyToReview_parentNotFound_returns404() throws Exception {
        // Arrange
        UUID parentId = UUID.randomUUID();
        CreateReviewRequest req = new CreateReviewRequest();
        req.setAuthorId(UUID.randomUUID());
        req.setOrganizationId(UUID.randomUUID());
        req.setTitle("Reply");
        req.setContent("C");
        req.setRatingValue(3);

        when(reviewService.createReview(
                any(), any(), any(), any(), eq(parentId), anyInt()
        )).thenThrow(new NoSuchElementException("not found"));

        // Act & Assert
        mockMvc.perform(post("/reviews/" + parentId + "/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1))
                .createReview(any(), any(), any(), any(), eq(parentId), anyInt());
    }

    @Test
    void addReaction_validLike_returnsOk() throws Exception {
        // Arrange
        UUID reviewId = UUID.randomUUID();
        AddReactionRequest req = new AddReactionRequest();
        req.setReactionType("LIKE");

        Review mockReview = Review.builder()
                .id(reviewId)
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("T")
                .content("C")
                .publishedAt(Instant.now())
                .ratingValue(1)
                .likeCount(1)
                .dislikeCount(0)
                .build();

        when(reviewService.addReaction(reviewId, "LIKE")).thenReturn(mockReview);

        // Act & Assert
        mockMvc.perform(post("/reviews/" + reviewId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1));

        verify(reviewService, times(1)).addReaction(reviewId, "LIKE");
    }

    @Test
    void addReaction_invalidType_returns400() throws Exception {
        // Arrange
        UUID reviewId = UUID.randomUUID();
        AddReactionRequest req = new AddReactionRequest();
        req.setReactionType("UNKNOWN");

        when(reviewService.addReaction(reviewId, "UNKNOWN"))
                .thenThrow(new IllegalArgumentException("Unknown reaction"));

        // Act & Assert
        mockMvc.perform(post("/reviews/" + reviewId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(reviewService, times(1)).addReaction(reviewId, "UNKNOWN");
    }

    @Test
    void getAllReviews_returnsList() throws Exception {
        // Arrange
        Review r1 = Review.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("A")
                .content("a")
                .publishedAt(Instant.now())
                .ratingValue(1)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        Review r2 = Review.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("B")
                .content("b")
                .publishedAt(Instant.now())
                .ratingValue(2)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewService.getAllReviews()).thenReturn(List.of(r1, r2));

        // Act & Assert
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(reviewService, times(1)).getAllReviews();
    }

    @Test
    void deleteReview_notFound_returns404() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        doThrow(new NoSuchElementException("not found"))
                .when(reviewService).deleteReview(id);

        // Act & Assert
        mockMvc.perform(delete("/reviews/" + id))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).deleteReview(id);
    }

    @Test
    void deleteReview_exists_returns204() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        doNothing().when(reviewService).deleteReview(id);

        // Act & Assert
        mockMvc.perform(delete("/reviews/" + id))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(id);
    }
}
