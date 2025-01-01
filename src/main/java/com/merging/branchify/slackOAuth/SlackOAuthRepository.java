package com.merging.branchify.slackOAuth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlackOAuthRepository extends JpaRepository<SlackOAuth, Long> {

    SlackOAuth findByWorkspaceId(String workspaceId);
}
