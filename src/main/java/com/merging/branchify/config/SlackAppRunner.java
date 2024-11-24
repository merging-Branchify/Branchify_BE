package com.merging.branchify.config;

import com.merging.branchify.controller.SlackEventController;
import com.slack.api.SlackConfig;
import com.slack.api.bolt.App;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Import(SlackConfig.class)
@Component // Spring Boot에서 이 클래스를 Bean으로 등록
public class SlackAppRunner implements CommandLineRunner {

    private final SlackEventController slackEventController;
    private final String appToken;

    public SlackAppRunner(SlackEventController slackEventController,
                          @Value("${slack.app.token}") String appToken) {
        this.slackEventController = slackEventController;
        this.appToken = appToken;

    }

    @Override
    public void run(String... args) throws Exception {

        // Slack Bolt App 설정 및 실행
        App app = new App();
        slackEventController.configureSlackCommands(app); // 명령어 및 핸들러 등록
        //WebSocket URL 가져오기
        String webSocketUrl = getWebSocketUrl();

        // WebSocket 연결 시작
        startWebSocketConnection(webSocketUrl, app);
//        SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
//        socketModeApp.start(); // Slack 앱 실행
    }
    /**
     * Slack API 호출을 통해 WebSocket URL 가져오기
     */
    private String getWebSocketUrl() {
        RestTemplate restTemplate = new RestTemplate();

        // API 호출 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(appToken);

        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        // Slack API 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                "https://slack.com/api/apps.connections.open",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                if (rootNode.get("ok").asBoolean()) {
                    return rootNode.get("url").asText();
                } else {
                    throw new RuntimeException("Slack API error: " + rootNode.get("error").asText());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse WebSocket URL from Slack response", e);
            }
        } else {
            throw new RuntimeException("Failed to retrieve WebSocket URL from Slack.");
        }
    }

    /**
     * WebSocket 연결 시작
     */
    private void startWebSocketConnection(String webSocketUrl, App app) {
        try {
            SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
            socketModeApp.start(); // WebSocket 연결 시작
            System.out.println("WebSocket connection established with URL: " + webSocketUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start WebSocket connection", e);
        }
    }
}