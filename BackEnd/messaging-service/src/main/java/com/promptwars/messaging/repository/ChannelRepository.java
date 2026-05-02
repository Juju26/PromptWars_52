package com.promptwars.messaging.repository;

import com.promptwars.messaging.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    List<Channel> findByTeamId(UUID teamId);

    List<Channel> findByType(Channel.ChannelType type);

    Optional<Channel> findByName(String name);

    boolean existsByName(String name);
}
