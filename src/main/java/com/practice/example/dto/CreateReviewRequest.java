package com.practice.example.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
public class CreateReviewRequest {
    @NonNull
    private UUID authorId;

    @NonNull
    private UUID organizationId;

    @NonNull
    private String title;

    private String content;

    private UUID parentReviewId;

    @NonNull
    private Integer ratingValue;
}
