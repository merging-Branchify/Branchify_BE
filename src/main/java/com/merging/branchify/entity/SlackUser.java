package com.merging.branchify.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SlackUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slack_workspace_id",nullable = false, unique = true)
    private String workspaceId; // Slack 워크스페이스 ID

    @Column(name = "slack_workspace_name")
    private String workspaceName; // Slack 워크스페이스 이름

    @Column(name = "slack_access_token")
    private String accessToken;

}
