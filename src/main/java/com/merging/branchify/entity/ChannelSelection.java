package com.merging.branchify.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChannelSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String channelId;

    // 기본 생성자
    public ChannelSelection() {}

    // 생성자
    public ChannelSelection(String userId, String channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

}