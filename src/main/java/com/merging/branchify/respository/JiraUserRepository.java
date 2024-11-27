package com.merging.branchify.respository;

import com.merging.branchify.entity.JiraUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraUserRepository extends JpaRepository<JiraUser, Long> {
    JiraUser findByUsername(String username);
}