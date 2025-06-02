package com.practice.example.service;

import com.practice.example.model.ReviewDetails;
import com.practice.example.model.ReviewRating;
import com.practice.example.model.ReviewReactions;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReviewService {
    private final Map<UUID, ReviewDetails> storage = new ConcurrentHashMap<>();

    public ReviewDetails createReview(UUID authorId,
                                      UUID organizationId,
                                      String title,
                                      String content,
                                      UUID parentReviewId,
                                      int ratingValue) {
        UUID newId = UUID.randomUUID();
        ReviewRating rating = new ReviewRating(ratingValue);
        Instant now = Instant.now();
        ReviewDetails details = new ReviewDetails(
                newId,
                authorId,
                organizationId,
                title,
                content,
                parentReviewId == null ? Optional.empty() : Optional.of(parentReviewId),
                now,
                rating
        );
        storage.put(newId, details);
        return details;
    }

    public ReviewDetails addReaction(UUID reviewId, String reactionTypeStr) {
        ReviewDetails details = storage.get(reviewId);
        if (details == null) {
            throw new NoSuchElementException("Review not found: " + reviewId);
        }
        ReviewReactions reaction = ReviewReactions.fromString(reactionTypeStr);
        details.addReaction(reaction);
        return details;
    }

    public Collection<ReviewDetails> getAllReviews() {
        return storage.values();
    }

    public void deleteReview(UUID reviewId) {
        storage.remove(reviewId);
    }
}
