package com.merging.branchify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.merging.branchify.dto.JiraProjectDTO;
import com.merging.branchify.dto.NotionDatabaseDTO;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class SlackService {
    private final NotionService notionService;
    private final JiraService jiraService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 온보딩 페이지
     */
    public boolean hasSeenOnboarding(String userId) {
        // TODO: DB 또는 캐시로 사용자 상태 확인 로직 구현
        return false; // 예시로 항상 false 반환
    }

    // 온보딩 상태 저장
    public void setOnboarded(String userId) {
        // TODO: 사용자 온보딩 상태 저장 로직 구현
    }

    // JSON 파일 로드
    public String loadOnboardingJson() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/resources/onboarding.json")));
        } catch (Exception e) {
            throw new RuntimeException("온보딩 JSON 파일 로드 실패", e);
        }
    }

    // 홈 탭 업데이트
    public void publishHomeTab(String userId, String viewJson) {
        String slackApiUrl = "https://slack.com/api/views.publish";

        // 요청 본문 생성
        Map<String, Object> requestBody = Map.of(
                "user_id", userId,
                "view", Map.of("type", "home", "blocks", viewJson)
        );

        // HTTP 요청
        restTemplate.postForEntity(
                slackApiUrl,
                new org.springframework.http.HttpEntity<>(requestBody, createHeaders()),
                String.class
        );
    }

    // HTTP 헤더 생성
    private org.springframework.http.HttpHeaders createHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_SLACK_BOT_TOKEN");
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * JSON 파일 경로
     **/
    private static final String NOTION_JSON_PATH = "src/main/resources/database_select.json";
    private static final String JIRA_JSON_PATH = "src/main/resources/project_select.json";

    public SlackService(NotionService notionService, JiraService jiraService) {
        this.notionService = notionService;
        this.jiraService = jiraService;
    }

    /**
     * 공통 JSON 업데이트 메서드
     * - Notion, Jira 데이터를 처리하여 JSON 파일 업데이트
     */
    private <T> void updateJson(List<T> data, String jsonPath,
                                java.util.function.Function<T, String> getTitle,
                                java.util.function.Function<T, String> getValue) throws IOException {

        // JSON 파일 읽기
        JsonNode rootNode = mapper.readTree(new File(jsonPath));
        JsonNode blocks = rootNode.get("blocks");

        JsonNode accessory = null;
        for (JsonNode block : blocks) {
            if (block.has("accessory")) {
                accessory = block.get("accessory");
                break;
            }
        }

        // 옵션 생성
        List<JsonNode> options = data.stream().map(item -> {
            JsonNode option = mapper.createObjectNode();
            ((ObjectNode) option).putObject("text")
                    .put("type", "plain_text")
                    .put("text", getTitle.apply(item));
            ((ObjectNode) option).put("value", getValue.apply(item));
            return option;
        }).toList();

        if (accessory != null) {
            ((ObjectNode) accessory).set("options", mapper.createArrayNode().addAll(options));
        }

        // JSON 파일 저장
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonPath), rootNode);
        System.out.println("Updated JSON at " + jsonPath + ": " + rootNode);
    }

    /**
     * Notion 데이터 JSON 업데이트
     */
    public void updateJsonWithDatabases() throws IOException {
        List<NotionDatabaseDTO> databases = notionService.listDatabases();
        updateJson(
                databases,
                NOTION_JSON_PATH,
                NotionDatabaseDTO::getTitle,  // 제목: Notion 데이터베이스의 제목
                NotionDatabaseDTO::getId     // 값: Notion 데이터베이스 ID
        );
    }

    /**
     * Jira 데이터 JSON 업데이트
     */
    public void updateJsonWithJiraProject() throws IOException {
        List<JiraProjectDTO> projects = jiraService.fetchProjectList();
        updateJson(
                projects,
                JIRA_JSON_PATH,
                JiraProjectDTO::getProjectName,  // 사용자에게 보이는 이름
                JiraProjectDTO::getProjectId    // 서버에서 처리할 값
        );
    }

    /**
     * Slack 메시지 전송
     */
    public void sendBlockMessage(String channelId, String botToken, String jsonPath, String message) throws IOException, SlackApiException {
        // JSON 파일 읽기
        JsonNode rootNode = mapper.readTree(new File(jsonPath));
        String blocksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode.get("blocks"));

        // Slack API 호출
        Slack slack = Slack.getInstance();
        var client = slack.methods(botToken);

        ChatPostMessageResponse response = client.chatPostMessage(req -> req
                .channel(channelId)
                .blocksAsString(blocksJson)
                .text(message)
        );

        if (!response.isOk()) {
            System.err.println("Slack API Error: " + response.getError());
        } else {
            System.out.println("Slack Message Sent Successfully!");
        }
    }

    /**
     * Notion 데이터 업데이트 + 메시지 전송
     */
    public void updateAndSendNotionMessage(String channelId, String botToken) throws IOException, SlackApiException {
        updateJsonWithDatabases();
        sendBlockMessage(channelId, botToken, NOTION_JSON_PATH, "알림을 받을 데이터베이스를 선택하세요.");
    }

    /**
     * Jira 데이터 업데이트 + 메시지 전송
     */
    public void updateAndSendJiraMessage(String channelId, String botToken) throws IOException, SlackApiException {
        updateJsonWithJiraProject();
        sendBlockMessage(channelId, botToken, JIRA_JSON_PATH, "알림을 받을 Jira 이슈를 선택하세요.");
    }

    /**
     * 채널 선택 전송 메서드
     */
    public void sendChannelSelectMessage(String channelId, String botToken) throws IOException, SlackApiException {
        // JSON 파일 읽기
        JsonNode rootNode = mapper.readTree(new File("src/main/resources/channel_select.json"));
        String blocksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode.get("blocks"));

        // Slack API 호출
        Slack slack = Slack.getInstance();
        var client = slack.methods(botToken);

        ChatPostMessageResponse response = client.chatPostMessage(req -> req
                .channel(channelId)
                .blocksAsString(blocksJson)
                .text("알림을 받을 Slack 채널을 선택하세요.")
        );

        if (!response.isOk()) {
            System.err.println("Slack API Error: " + response.getError());
        } else {
            System.out.println("Slack Channel Selection Message Sent Successfully!");
        }
    }
}
