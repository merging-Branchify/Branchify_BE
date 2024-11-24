package com.merging.branchify.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "jira_project_selection")
public class JiraProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId; // Slack 사용자 ID

    @Column(nullable = false)
    private String projectId; // 선택된 Jira 프로젝트 ID
}
