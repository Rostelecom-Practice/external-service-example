package com.practice.example.service;

import com.practice.example.model.Review;
import com.practice.example.model.ReviewDetails;
import com.practice.example.model.ReviewRating;
import com.practice.example.model.ReviewReactions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDetailsProvider {

    public ReviewDetails getDetailsTo(Review review) {
        ReviewDetails reviewDetailsWithoutReactions = ReviewDetails.builder()
                .id(review.getId())
                .authorId(review.getAuthorId())
                .title(review.getTitle())
                .content(review.getContent())
                .parentReviewId(Objects.isNull(review.getParentReview()) ?
                        Optional.empty() :
                        Optional.ofNullable(review.getParentReview().getId()))
                .publishedAt(review.getPublishedAt())
                .rating(new ReviewRating(review.getRatingValue()))
                .organizationId(review.getOrganizationId())
                .build();

         reviewDetailsWithoutReactions.addReaction(ReviewReactions.Type.LIKE, review.getLikeCount());
         reviewDetailsWithoutReactions.addReaction(ReviewReactions.Type.DISLIKE, review.getDislikeCount());

         return reviewDetailsWithoutReactions;
    }

}

