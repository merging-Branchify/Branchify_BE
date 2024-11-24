package com.merging.branchify.respository;

import com.merging.branchify.entity.JiraProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraProjectRepository extends JpaRepository <JiraProject, Long>{
}
