package com.merging.branchify.jiraOAuth;

import com.merging.branchify.slackOAuth.SlackOAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JiraOAuthRepository extends JpaRepository<JiraOAuth, Long> {
    List<JiraOAuth> findBySlackWorkspace(SlackOAuth slackWorkspace);
}
