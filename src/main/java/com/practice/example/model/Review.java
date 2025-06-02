package com.practice.example.model;

import lombok.*;
import jakarta.persistence.*;    // Пакеты Jakarta Persistence
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Ответ на другой отзыв (self-join)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_review_id")
    private Review parentReview;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "rating_value", nullable = false)
    private int ratingValue;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    private int dislikeCount = 0;

    // Конструктор без parentReview, если нужно
    public Review(UUID id,
                  UUID authorId,
                  UUID organizationId,
                  String title,
                  String content,
                  Instant publishedAt,
                  int ratingValue) {
        this.id = id;
        this.authorId = authorId;
        this.organizationId = organizationId;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.ratingValue = ratingValue;
    }
}
