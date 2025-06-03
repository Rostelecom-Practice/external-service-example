package com.practice.example.service;

import com.practice.example.infra.kafka.ReviewKafkaProducer;
import com.practice.example.model.Review;
import com.practice.example.model.ReviewDetails;
import com.practice.example.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private ReviewService reviewService;
    private ReviewDetailsProvider reviewDetailsProvider;
    private ReviewKafkaProducer reviewKafkaProducer;

    @BeforeEach
    void setUp() {
        reviewRepository = Mockito.mock(ReviewRepository.class);
        reviewDetailsProvider = Mockito.mock(ReviewDetailsProvider.class);
        reviewKafkaProducer = Mockito.mock(ReviewKafkaProducer.class);
        reviewService = new ReviewService(reviewRepository, reviewDetailsProvider, reviewKafkaProducer);
    }

    @Test
    void createReview_withoutParent_savesNewEntity() {
        UUID authorId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String title = "Test";
        String content = "Content";
        int ratingValue = 5;
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));



        Review saved = reviewService.createReview(
                authorId,
                orgId,
                title,
                content,
                null,
                ratingValue
        );

        ReviewDetails newReviewDetails = new ReviewDetails(saved.getId(), authorId, orgId, title, content,
                Optional.empty(),
                saved.getPublishedAt());

        when(reviewDetailsProvider.getDetailsTo(saved))
                .thenReturn(newReviewDetails);

        assertThat(saved.getAuthorId()).isEqualTo(authorId);
        assertThat(saved.getOrganizationId()).isEqualTo(orgId);
        assertThat(saved.getTitle()).isEqualTo(title);
        assertThat(saved.getContent()).isEqualTo(content);
        assertThat(saved.getRatingValue()).isEqualTo(ratingValue);
        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getDislikeCount()).isZero();
        assertThat(saved.getPublishedAt()).isNotNull();

        verify(reviewRepository, times(1)).save(saved);
        verify(reviewDetailsProvider, times(1)).getDetailsTo(saved);
        verify(reviewKafkaProducer, times(1)).sendReviewPublished(any());
    }

    @Test
    void createReview_withNonexistentParent_throwsException() {
        UUID parentId = UUID.randomUUID();
        when(reviewRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Title",
                "Text",
                parentId,
                3
        )).isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Parent review not found");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_withExistingParent_setsParentReference() {
        UUID parentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        Review parent = Review.builder()
                .id(parentId)
                .authorId(authorId)
                .organizationId(orgId)
                .title("Parent")
                .content("P")
                .publishedAt(Instant.now())
                .ratingValue(4)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review child = reviewService.createReview(
                authorId,
                orgId,
                "Child",
                "C",
                parentId,
                2
        );

        assertThat(child.getParentReview()).isNotNull();
        assertThat(child.getParentReview().getId()).isEqualTo(parentId);
        verify(reviewRepository, times(1)).save(child);
    }

    @Test
    void addReaction_like_incrementsLikeCount() {
        UUID reviewId = UUID.randomUUID();
        Review existing = Review.builder()
                .id(reviewId)
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("T")
                .content("C")
                .publishedAt(Instant.now())
                .ratingValue(1)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review updated = reviewService.addReaction(reviewId, "LIKE");

        assertThat(updated.getLikeCount()).isEqualTo(1);
        assertThat(updated.getDislikeCount()).isZero();
        verify(reviewRepository, times(1)).save(updated);
        verify(reviewKafkaProducer, never()).sendReviewPublished(any());
    }

    @Test
    void addReaction_dislike_incrementsDislikeCount() {
        UUID reviewId = UUID.randomUUID();
        Review existing = Review.builder()
                .id(reviewId)
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("T")
                .content("C")
                .publishedAt(Instant.now())
                .ratingValue(1)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review updated = reviewService.addReaction(reviewId, "DISLIKE");

        assertThat(updated.getDislikeCount()).isEqualTo(1);
        assertThat(updated.getLikeCount()).isZero();
        verify(reviewRepository, times(1)).save(updated);
    }

    @Test
    void addReaction_unknownType_throwsIllegalArgument() {
        UUID reviewId = UUID.randomUUID();
        Review existing = Review.builder()
                .id(reviewId)
                .authorId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .title("T")
                .content("C")
                .publishedAt(Instant.now())
                .ratingValue(1)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.addReaction(reviewId, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown reaction");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReaction_nonexistentId_throwsNoSuchElement() {
        UUID missingId = UUID.randomUUID();
        when(reviewRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReaction(missingId, "LIKE"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Review not found");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_existing_removesEntity() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(reviewId);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).deleteById(reviewId);
    }

    @Test
    void deleteReview_nonexistent_throwsNoSuchElement() {
        UUID missingId = UUID.randomUUID();
        when(reviewRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.deleteReview(missingId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Review not found");
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void getAllReviews_delegatesToRepository() {
        when(reviewRepository.findAll()).thenReturn(java.util.List.of());
        var list = reviewService.getAllReviews();
        assertThat(list).isEmpty();
        verify(reviewRepository, times(1)).findAll();
    }
}
