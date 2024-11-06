package com.merging.branchify.controller;

import com.merging.branchify.service.JiraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jira")
public class JiraController {

    private final JiraService jiraService;

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @GetMapping("/fetch-updates")
    public String fetchJiraUpdates() {
        jiraService.fetchAndStoreJiraUpdates();
        return "Jira updates have been fetched and stored successfully.";
    }
}