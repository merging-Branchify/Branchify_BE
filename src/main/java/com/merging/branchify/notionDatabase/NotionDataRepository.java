package com.merging.branchify.notionDatabase;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotionDataRepository extends JpaRepository<NotionDatabase, Long> {
}
