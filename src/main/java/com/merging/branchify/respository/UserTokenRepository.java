package com.merging.branchify.respository;

import com.merging.branchify.entity.NotionDatabase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<NotionDatabase, Long> {
}
