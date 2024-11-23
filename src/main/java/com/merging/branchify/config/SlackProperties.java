package com.merging.branchify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

    private Client client;
    private Redirect redirect;

    @Data
    public static class Client {
        private String id;
        private String secret;
    }

    @Data
    public static class Redirect {
        private String uri;
    }
}
