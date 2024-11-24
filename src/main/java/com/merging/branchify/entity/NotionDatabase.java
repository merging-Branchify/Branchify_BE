package com.merging.branchify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@Table(name = "notion_database")
@AllArgsConstructor
@NoArgsConstructor
public class NotionDatabase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId; // 사용자 ID

    @Column(nullable = false)
    private String selectedDatabaseId; // 선택된 데이터베이스 ID

    @Column(nullable = false)
    private Instant lastChecked; // 마지막으로 확인한 시간
}
