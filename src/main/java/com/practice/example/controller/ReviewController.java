package com.practice.example.controller;

import com.practice.example.dto.AddReactionRequest;
import com.practice.example.dto.CreateReviewRequest;
import com.practice.example.model.ReviewDetails;
import com.practice.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;


@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDetails> createReview(@RequestBody CreateReviewRequest request) {
        ReviewDetails created = reviewService.createReview(
                request.getAuthorId(),
                request.getOrganizationId(),
                request.getTitle(),
                request.getContent(),
                request.getParentReviewId(),
                request.getRatingValue()
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ReviewDetails> replyToReview(@PathVariable("id") UUID reviewId,
                                                       @RequestBody CreateReviewRequest request) {
        ReviewDetails reply = reviewService.createReview(
                request.getAuthorId(),
                request.getOrganizationId(),
                request.getTitle(),
                request.getContent(),
                reviewId,
                request.getRatingValue()
        );
        return ResponseEntity.ok(reply);
    }

    @PostMapping("/{id}/reactions")
    public ResponseEntity<ReviewDetails> addReaction(@PathVariable("id") UUID reviewId,
                                                     @RequestBody AddReactionRequest request) {
        try {
            ReviewDetails updated = reviewService.addReaction(reviewId, request.getReactionType());
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Collection<ReviewDetails>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") UUID reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
