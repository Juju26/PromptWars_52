package com.promptwars.messaging.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes event notifications to Google Cloud Pub/Sub.
 * Used for: new messages, task updates, organiser announcements.
 * Publishing is non-blocking — uses CompletableFuture to avoid blocking servlet threads.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pubsub.topic.notifications:hackathon-notifications}")
    private String notificationsTopic;

    @Value("${pubsub.topic.announcements:hackathon-announcements}")
    private String announcementsTopic;

    /**
     * Publishes a new-message event so connected clients can poll or SSE listeners can push.
     */
    public void publishNewMessage(UUID channelId, String senderUsername, String preview) {
        Map<String, Object> event = Map.of(
                "eventType", "NEW_MESSAGE",
                "channelId", channelId.toString(),
                "senderUsername", senderUsername,
                "preview", truncate(preview, 100),
                "timestamp", java.time.Instant.now().toString()
        );
        publishAsync(notificationsTopic, event);
    }

    /**
     * Publishes an organiser announcement to the announcements topic.
     * All daemon subscribers receive this for push-style notifications.
     */
    public void publishAnnouncement(String title, String body) {
        Map<String, Object> event = Map.of(
                "eventType", "ANNOUNCEMENT",
                "title", title,
                "body", body,
                "timestamp", java.time.Instant.now().toString()
        );
        publishAsync(announcementsTopic, event);
    }

    public void publishTaskUpdate(UUID taskId, String newStatus, UUID assigneeId) {
        Map<String, Object> event = Map.of(
                "eventType", "TASK_UPDATE",
                "taskId", taskId.toString(),
                "newStatus", newStatus,
                "assigneeId", assigneeId != null ? assigneeId.toString() : "",
                "timestamp", java.time.Instant.now().toString()
        );
        publishAsync(notificationsTopic, event);
    }

    private void publishAsync(String topic, Object payload) {
        CompletableFuture.runAsync(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                pubSubTemplate.publish(topic, json);
                log.debug("Published to topic={}: {}", topic, json);
            } catch (Exception e) {
                log.error("Failed to publish to Pub/Sub topic={}: {}", topic, e.getMessage());
            }
        });
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // Java import needed for UUID
    private java.util.UUID UUID(String s) { return java.util.UUID.fromString(s); }
}
