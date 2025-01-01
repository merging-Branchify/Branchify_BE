package com.merging.branchify.jiraOAuth;

import com.merging.branchify.slackOAuth.SlackOAuth;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "jira_oauth")
public class JiraOAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "slack_workspace_id", referencedColumnName = "slack_workspace_id", nullable = false)
    private SlackOAuth slackWorkspace;

    @Column(name = "jira_project_id", nullable = false)
    private String projectId;

    @Column(name = "jira_project_name", nullable = false)
    private String projectName;

    @Column(name = "jira_project_url", nullable = false)
    private String projectUrl;

    @Column(name = "jira_access_token", nullable = false)
    private String accessToken;

    @Column(name = "jira_email", nullable = false)
    private String email;

    @Column(name = "jira_project_key", nullable = false)
    private String projectKey;
}
