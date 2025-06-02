package com.practice.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ReviewReactions {

    public static enum Type {
        LIKE, DISLIKE, EMOJI
    }

    private final char value;
    private final Type type;

    private ReviewReactions(char value, Type type) {
        this.value = value;
        this.type = type;
    }

    public ReviewReactions(char value) {
        this(value, Type.EMOJI);
    }

    private static final ReviewReactions LIKE_INSTANCE = new ReviewReactions('L', Type.LIKE);
    private static final ReviewReactions DISLIKE_INSTANCE = new ReviewReactions('D', Type.DISLIKE);

    public static ReviewReactions like() {
        return LIKE_INSTANCE;
    }

    public static ReviewReactions dislike() {
        return DISLIKE_INSTANCE;
    }

    public static ReviewReactions fromString(String name) {
        return switch (name.toUpperCase()) {
            case "LIKE" -> like();
            case "DISLIKE" -> dislike();
            default -> throw new IllegalArgumentException("Unknown reaction: " + name);
        };
    }

}