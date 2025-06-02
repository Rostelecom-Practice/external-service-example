package com.practice.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ReviewDetails {
    private final UUID id;
    private final UUID authorId;
    private final UUID organizationId;
    private final String title;
    private final String content;
    private final Map<ReviewReactions.Type, Integer> reactions = new EnumMap<>(ReviewReactions.Type.class);
    private final Optional<UUID> parentReviewId;
    private final Instant publishedAt;
    private ReviewRating rating;

    public ReviewDetails(UUID id,
                         UUID authorId,
                         UUID organizationId,
                         String title,
                         String content,
                         Optional<UUID> parentReviewId,
                         Instant publishedAt,
                         ReviewRating rating) {
        this.id = id;
        this.authorId = authorId;
        this.organizationId = organizationId;
        this.title = title;
        this.content = content;
        this.parentReviewId = parentReviewId;
        this.publishedAt = publishedAt;
        this.rating = rating;
        reactions.put(ReviewReactions.Type.LIKE, 0);
        reactions.put(ReviewReactions.Type.DISLIKE, 0);
    }

    public synchronized void addReaction(ReviewReactions.Type reactionType) {
        reactions.merge(reactionType, 1, Integer::sum);
    }

    public synchronized void setRating(ReviewRating newRating) {
        this.rating = newRating;
    }
}