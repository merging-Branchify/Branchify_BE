package com.merging.branchify.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JiraScheduler {

    private final JiraService jiraService;

    public JiraScheduler(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    // 매일 오전 3시에 Jira 변경 사항을 자동으로 가져오기
    @Scheduled(cron = "0 0 9,17 * * ?")
    public void fetchJiraUpdates() {
        System.out.println("Jira 업데이트 작업 시작");
        jiraService.fetchAndStoreJiraUpdates();
        System.out.println("Jira 업데이트 작업 완료");
    }
}
