package com.merging.branchify.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SlackUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workspaceId;

    private String accessToken;

}
