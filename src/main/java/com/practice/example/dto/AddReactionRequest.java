package com.practice.example.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class AddReactionRequest {

    @NonNull
    private String reactionType;
}
