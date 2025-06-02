package com.practice.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReviewRating {
    private final int value;

    public double getValue(double base) {
        return value / base;
    }

    public double getValueBase5() {
        return value / 5.0;
    }
}
