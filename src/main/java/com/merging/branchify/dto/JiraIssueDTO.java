package com.merging.branchify.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JiraIssueDTO {
    private String issueId;
    private String issueTitle;
    private String summary;
    private String status;
    private String assignee;
    private LocalDateTime updatedAt;
    private String projectKey;
}
