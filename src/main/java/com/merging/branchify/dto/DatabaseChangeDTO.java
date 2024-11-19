package com.merging.branchify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DatabaseChangeDTO {

    private String id; // 데이터베이스 내 변경된 페이지 ID
    private Map<String, Object> properties; // 페이지의 속성 정보
}
