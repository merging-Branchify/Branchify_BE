package com.merging.branchify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraUserDTO {
    private String username;
    private String jiraBaseUrl;
    private String accessToken;
    private String refreshToken;
}
