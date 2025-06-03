package com.practice.example.service;

import com.practice.example.dto.CreateReviewRequest;
import com.practice.example.infra.kafka.ReviewKafkaProducer;
import com.practice.example.model.Review;
import com.practice.example.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final ReviewDetailsProvider reviewDetailsProvider;

    private final ReviewKafkaProducer reviewKafkaProducer;

    @Transactional
    public Review createReview(UUID authorId,
                               UUID organizationId,
                               String title,
                               String content,
                               UUID parentReviewId,
                               int ratingValue) {
        UUID newId = UUID.randomUUID();
        Review parent = null;
        if (parentReviewId != null) {
            parent = reviewRepository.findById(parentReviewId)
                    .orElseThrow(() -> new NoSuchElementException("Parent review not found: " + parentReviewId));
        }

        Review review = Review.builder()
                .id(newId)
                .authorId(authorId)
                .organizationId(organizationId)
                .title(title)
                .content(content)
                .parentReview(parent)
                .publishedAt(Instant.now())
                .ratingValue(ratingValue)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        reviewKafkaProducer.sendReviewPublished(reviewDetailsProvider.getDetailsTo(review));
        return reviewRepository.save(review);
    }

    @Transactional
    public Review addReaction(UUID reviewId, String reactionTypeStr) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

        // Можно добавить поддержку других типов реакций, здесь оставляем только LIKE/DISLIKE
        switch (reactionTypeStr.toUpperCase()) {
            case "LIKE":
                review.setLikeCount(review.getLikeCount() + 1);
                break;
            case "DISLIKE":
                review.setDislikeCount(review.getDislikeCount() + 1);
                break;
            default:
                throw new IllegalArgumentException("Unknown reaction: " + reactionTypeStr);
        }

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new NoSuchElementException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
}
