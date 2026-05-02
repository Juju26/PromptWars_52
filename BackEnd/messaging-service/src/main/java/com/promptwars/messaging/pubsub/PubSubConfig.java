package com.promptwars.messaging.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Map;

/**
 * Configures an inbound Pub/Sub subscription listener.
 * Handles incoming announcement events from organiser tools or external webhooks.
 * Uses Manual ACK mode to avoid message loss on processing failures.
 */
@Slf4j
@Configuration
public class PubSubConfig {

    @Value("${pubsub.subscription.announcements:hackathon-announcements-sub}")
    private String announcementsSubscription;

    @Bean
    public MessageChannel announcementsInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter announcementsAdapter(
            PubSubTemplate pubSubTemplate,
            MessageChannel announcementsInputChannel
    ) {
        PubSubInboundChannelAdapter adapter =
                new PubSubInboundChannelAdapter(pubSubTemplate, announcementsSubscription);
        adapter.setOutputChannel(announcementsInputChannel);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "announcementsInputChannel")
    public MessageHandler announcementsMessageHandler(ObjectMapper objectMapper) {
        return message -> {
            BasicAcknowledgeablePubsubMessage original =
                    message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE,
                            BasicAcknowledgeablePubsubMessage.class);
            try {
                String payload = new String((byte[]) message.getPayload());
                Map<?, ?> event = objectMapper.readValue(payload, Map.class);
                log.info("Received Pub/Sub announcement: type={}", event.get("eventType"));
                // Extend here: forward to WebSocket broker, push to SSE streams, etc.
                if (original != null) original.ack();
            } catch (Exception e) {
                log.error("Failed to process announcement event: {}", e.getMessage());
                if (original != null) original.nack();
            }
        };
    }
}
