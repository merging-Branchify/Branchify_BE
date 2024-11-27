package com.merging.branchify.slackBot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<ChannelSelection, Long> {
    ChannelSelection findByUserId(String userId);
}
