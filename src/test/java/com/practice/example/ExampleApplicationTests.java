package com.practice.example;

import com.practice.example.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ExampleApplicationTests {

	@Autowired
	private ReviewService reviewService;

	@Test
	void contextLoads() {
		assertThat(reviewService).isNotNull();
	}
}
