package com.practice.example.infra.kafka;

import com.practice.example.model.Review;
import com.practice.example.model.ReviewDetails;
import com.practice.example.model.ReviewReactions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class ReviewKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${review.kafka.topic}")
    private String topic;

    public void sendReviewPublished(ReviewDetails details) {
        String key = details.getOrganizationId().toString();
        String json = serializeToJson(details);
        kafkaTemplate.send(topic, key, json);
    }


    private String serializeToJson(ReviewDetails review) {
        StringBuilder sb = new StringBuilder("{");

        sb.append("\"id\":\"").append(review.getId()).append("\",");
        sb.append("\"authorId\":\"").append(review.getAuthorId()).append("\",");
        sb.append("\"organizationId\":\"").append(review.getOrganizationId()).append("\",");
        sb.append("\"title\":").append(escapeJson(review.getTitle())).append(",");
        sb.append("\"content\":").append(escapeJson(review.getContent())).append(",");
        sb.append("\"publishedAt\":\"").append(review.getPublishedAt()).append("\",");

        sb.append("\"rating\":\"").append(review.getRating().toString()).append("\",");

        sb.append("\"reactions\":").append(serializeReactions(review.getReactions())).append(",");

        sb.append("\"parentReviewId\":");
        sb.append(review.getParentReviewId()
                .map(uuid -> "\"" + uuid + "\"")
                .orElse("null"));

        sb.append("}");

        return sb.toString();
    }

    private String serializeReactions(Map<ReviewReactions.Type, Integer> reactions) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<ReviewReactions.Type, Integer> entry : reactions.entrySet()) {
            if (!first) sb.append(",");
            switch (entry.getKey()) {
                case LIKE:
                    sb.append("\"").append(ReviewReactions.like()).append("\":").append(entry.getValue());
                    break;
                case DISLIKE:
                    sb.append("\"").append(ReviewReactions.dislike()).append("\":").append(entry.getValue());
                    break;
            }

            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String raw) {
        if (raw == null) return "null";
        return "\"" + raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

}
