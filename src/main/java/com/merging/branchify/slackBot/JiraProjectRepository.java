package com.merging.branchify.slackBot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraProjectRepository extends JpaRepository <JiraProject, Long>{
}
