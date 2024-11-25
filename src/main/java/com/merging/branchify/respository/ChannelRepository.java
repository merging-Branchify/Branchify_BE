package com.merging.branchify.respository;

import com.merging.branchify.entity.ChannelSelection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<ChannelSelection, Long> {
    ChannelSelection findByUserId(String userId);
}
