package com.merging.branchify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Table(name = "user_tokens")
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String selectedDatabaseId; // 선택된 데이터베이스 ID

    @Column(nullable = false)
    private Instant lastChecked; // 마지막으로 확인한 시간
}
