package com.merging.branchify.notionDatabase;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotionDatabaseDTO {

    private String id; // 데이터베이스 ID
    private String title; // 데이터베이스 제목
}
