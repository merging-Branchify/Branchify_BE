package com.merging.branchify.respository;

import com.merging.branchify.entity.SlackUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {

    SlackUser findByWorkspaceId(String workspaceId);
}
