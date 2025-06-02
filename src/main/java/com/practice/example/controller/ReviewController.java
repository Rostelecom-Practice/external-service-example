package com.practice.example.controller;

import com.practice.example.dto.AddReactionRequest;
import com.practice.example.dto.CreateReviewRequest;
import com.practice.example.model.Review;
import com.practice.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<Review> createReview(@RequestBody CreateReviewRequest request) {
        Review created = reviewService.createReview(
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
    public ResponseEntity<Review> replyToReview(@PathVariable("id") UUID reviewId,
                                                @RequestBody CreateReviewRequest request) {
        try {
            Review reply = reviewService.createReview(
                    request.getAuthorId(),
                    request.getOrganizationId(),
                    request.getTitle(),
                    request.getContent(),
                    reviewId,
                    request.getRatingValue()
            );
            return ResponseEntity.ok(reply);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reactions")
    public ResponseEntity<Review> addReaction(@PathVariable("id") UUID reviewId,
                                              @RequestBody AddReactionRequest request) {
        try {
            Review updated = reviewService.addReaction(reviewId, request.getReactionType());
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") UUID reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
