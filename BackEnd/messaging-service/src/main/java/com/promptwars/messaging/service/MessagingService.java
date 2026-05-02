package com.promptwars.messaging.service;

import com.promptwars.common.exception.BusinessException;
import com.promptwars.common.exception.ResourceNotFoundException;
import com.promptwars.messaging.domain.Channel;
import com.promptwars.messaging.domain.Message;
import com.promptwars.messaging.dto.MessagingDtos;
import com.promptwars.messaging.pubsub.EventPublisher;
import com.promptwars.messaging.repository.ChannelRepository;
import com.promptwars.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public MessagingDtos.ChannelResponse createChannel(MessagingDtos.CreateChannelRequest req, UUID creatorId) {
        if (channelRepository.existsByName(req.name())) {
            throw new BusinessException("Channel name already exists: " + req.name());
        }
        Channel channel = Channel.builder()
                .name(req.name())
                .description(req.description())
                .type(req.type() != null ? req.type() : Channel.ChannelType.TEAM)
                .teamId(req.teamId())
                .createdBy(creatorId)
                .build();
        channel = channelRepository.save(channel);
        log.info("Channel created: id={} name={}", channel.getId(), channel.getName());
        return toChannelResponse(channel);
    }

    @Transactional(readOnly = true)
    public List<MessagingDtos.ChannelResponse> getTeamChannels(UUID teamId) {
        return channelRepository.findByTeamId(teamId)
                .stream().map(this::toChannelResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessagingDtos.ChannelResponse> getPublicChannels() {
        return channelRepository.findByType(Channel.ChannelType.GENERAL)
                .stream().map(this::toChannelResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessagingDtos.MessageResponse sendMessage(
            UUID channelId, MessagingDtos.SendMessageRequest req,
            UUID senderId, String senderUsername) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));

        // ANNOUNCEMENT channels: only organizers can post (role enforcement done in controller via @PreAuthorize)
        Message message = Message.builder()
                .channelId(channelId)
                .senderId(senderId)
                .content(req.content())
                .senderUsername(senderUsername)
                .build();
        message = messageRepository.save(message);

        // Async fire-and-forget — never blocks the HTTP response
        eventPublisher.publishNewMessage(channelId, senderUsername, req.content());

        return toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public List<MessagingDtos.MessageResponse> getMessages(UUID channelId, Instant before, int limit) {
        channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));
        return messageRepository.findByChannelIdCursor(channelId, before, PageRequest.of(0, limit))
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessage(UUID messageId, UUID requesterId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        if (!msg.getSenderId().equals(requesterId)) {
            throw new BusinessException("You can only delete your own messages");
        }
        msg.setDeletedAt(Instant.now());
        messageRepository.save(msg);
    }

    @Transactional
    public void publishAnnouncement(MessagingDtos.AnnouncementRequest req) {
        eventPublisher.publishAnnouncement(req.title(), req.body());
        log.info("Announcement published: title={}", req.title());
    }

    private MessagingDtos.ChannelResponse toChannelResponse(Channel c) {
        return new MessagingDtos.ChannelResponse(c.getId(), c.getName(), c.getDescription(),
                c.getType(), c.getTeamId(), c.getCreatedAt());
    }

    private MessagingDtos.MessageResponse toMessageResponse(Message m) {
        return new MessagingDtos.MessageResponse(m.getId(), m.getChannelId(), m.getSenderId(),
                m.getSenderUsername(), m.getContent(), m.getCreatedAt());
    }
}
