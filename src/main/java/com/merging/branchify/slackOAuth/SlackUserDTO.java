package com.merging.branchify.slackOAuth;

import lombok.Data;

@Data
public class SlackUserDTO {
    private String workspaceId;
    private String workspaceName;
    private String accessToken;
}