package com.merging.branchify.slackBot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class JiraProjectDTO {
    private String ProjectId;      // 프로젝트 ID (내부 식별자)
    private String key;     // 프로젝트 키 (Jira에서 프로젝트 식별)
    private String ProjectName;    // 프로젝트 이름 (사용자에게 표시)
    private String type;    // 프로젝트 유형 (예: software, business)

    public JiraProjectDTO(String id, String key, String name, String type) {
        this.ProjectId = id;
        this.key = key;
        this.ProjectName = name;
        this.type = type;
    }
}