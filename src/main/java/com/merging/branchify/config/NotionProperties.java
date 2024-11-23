package com.merging.branchify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "notion")
public class NotionProperties {

    private Client client;       // 클라이언트 정보
    private Redirect redirect;   // 리다이렉트 URI

    @Data
    public static class Client {
        private String id;       // Notion OAuth 클라이언트 ID
        private String secret;   // Notion OAuth 클라이언트 Secret
    }

    @Data
    public static class Redirect {
        private String uri;      // Notion OAuth 리다이렉트 URI
    }
}
