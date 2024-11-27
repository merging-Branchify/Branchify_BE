package com.merging.branchify.slackOAuth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackUserRepository extends JpaRepository<SlackOAuth, Long> {

    SlackOAuth findByWorkspaceId(String workspaceId);
}
