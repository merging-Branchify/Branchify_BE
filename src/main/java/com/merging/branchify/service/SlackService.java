package com.merging.branchify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.merging.branchify.dto.JiraIssueDTO;
import com.merging.branchify.dto.NotionDatabaseDTO;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class SlackService {
    private final NotionService notionService;
    private final JiraService jiraService;
    private final ObjectMapper mapper = new ObjectMapper();

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
    public void updateJsonWithJiraIssue() throws IOException {
        List<JiraIssueDTO> issues = jiraService.fetchAndReturnIssuesIds();
        updateJson(
                issues,
                JIRA_JSON_PATH,
                JiraIssueDTO::getIssueId,  // 제목: Jira 이슈 ID
                JiraIssueDTO::getIssueId   // 값: Jira 이슈 ID (제목과 동일)
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
        updateJsonWithJiraIssue();
        sendBlockMessage(channelId, botToken, JIRA_JSON_PATH, "알림을 받을 Jira 이슈를 선택하세요.");
    }
}
//@Service
//public class SlackService {
//    private final NotionService notionService;
//    private final JiraService jiraService;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    /**
//    JSON 파일 경로
//     **/
//    private static final String NOTION_JSON_PATH = "src/main/resources/database_select.json";
//    private static final String CHANNEL_JSON_FILE_PATH = "src/main/resources/channel_select.json";
//    private static final String JIRA_JSON_PATH = "src/main/resources/project_select.json";
//
//    public SlackService(NotionService notionService, JiraService jiraService) {
//        this.notionService = notionService;
//        this.jiraService = jiraService;
//    }
//
//
//    /**
//     * Notion 데이터를 JSON 파일에 추가 (드롭다운 옵션 업데이트)
//     * */
//    public void updateJsonWithDatabases() throws IOException {
//        List<NotionDatabaseDTO> databases = notionService.listDatabases();
//
//        // JSON 파일 읽기
//        JsonNode rootNode = mapper.readTree(new File(NOTION_JSON_PATH));
//        JsonNode blocks = rootNode.get("blocks");
//
//        JsonNode accessory= null;
//        for (JsonNode block : blocks) {
//            if (block.has("accessory")) {
//                accessory = block.get("accessory");
//                break;
//            }
//        }
//
//        // 기존 옵션 대체
//        List<JsonNode> options = databases.stream().map(database -> {
//            JsonNode option = mapper.createObjectNode();
//            ((ObjectNode) option).putObject("text")
//                    .put("type", "plain_text")
//                    .put("text", database.getTitle());
//            ((ObjectNode) option).put("value", database.getId());
//            return option;
//        }).toList();
//
//        ((ObjectNode) accessory).set("options", mapper.createArrayNode().addAll(options));
//
//        // JSON 파일 저장
//        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(NOTION_JSON_PATH), rootNode);
//        System.out.println("Updated JSON: " + rootNode);
//    }
//
//    /**
//     * jira 데이터 JSON에 전송
//     */
//    public void updateJsonWithJiraIssue() throws IOException {
//        List<JiraIssueDTO> issues = jiraService.fetchAndReturnIssuesIds();
//
//        // JSON 파일 읽기
//        JsonNode rootNode = mapper.readTree(new File(JIRA_JSON_PATH));
//        JsonNode blocks = rootNode.get("blocks");
//
//        JsonNode accessory= null;
//        for (JsonNode block : blocks) {
//            if (block.has("accessory")) {
//                accessory = block.get("accessory");
//                break;
//            }
//        }
//
//        List<String> issueIds = jiraService.fetchAndReturnIssuesIds().stream()
//                .map(JiraIssueDTO::getIssueId) // DTO에서 이슈 ID만 추출
//                .toList();
//
//        // 옵션 생성
//        List<JsonNode> options = issues.stream().map(issue -> {
//            JsonNode option = mapper.createObjectNode();
//            ((ObjectNode) option).putObject("text")
//                    .put("type", "plain_text")
//                    .put("text", issue.getIssueId());
//            ((ObjectNode) option).put("value", issue.getIssueId());
//            return option;
//        }).toList();
//
//        ((ObjectNode) accessory).set("options", mapper.createArrayNode().addAll(options));
//
//        // JSON 파일 저장
//        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(JIRA_JSON_PATH), rootNode);
//        System.out.println("Updated JSON: " + rootNode);
//    }
//    /**
//     * Slack 메시지 전송
//     */
//    public void sendBlockMessage(String channelId, String botToken) throws IOException, SlackApiException {
//        // JSON 파일 읽기
//        JsonNode rootNode = mapper.readTree(new File(NOTION_JSON_PATH));
//        String blocksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode.get("blocks"));
//
//        // Slack API 호출
//        Slack slack = Slack.getInstance();
//        var client = slack.methods(botToken);
//
//        ChatPostMessageResponse response = client.chatPostMessage(req -> req
//                .channel(channelId)
//                .blocksAsString(blocksJson) // JSON 블록 전송
//                .text("알림을 받을 데이터베이스를 선택하세요.") // 텍스트 필드 추가
//        );
//
//        if (!response.isOk()) {
//            System.err.println("Slack API Error: " + response.getError());
//        } else {
//            System.out.println("Slack Message Sent Successfully!");
//        }
//    }
//
//    /**
//     * json 업데이트 + 메세지 전송
//     */
//    public void updateAndSendMessage(String channelId, String botToken) throws IOException, SlackApiException {
//        // JSON 파일 업데이트
//        updateJsonWithDatabases();
//
//        // Slack 메시지 전송
//        sendBlockMessage(channelId, botToken);
//    }
//
//
//    /**
//     * 알림 채널 선택
//     */
//
//    public void sendChannelSelectionMessage(String channelId, String botToken) throws IOException, SlackApiException {
//        // Step 1: JSON 파일 읽기
//        JsonNode rootNode = mapper.readTree(new File("src/main/resources/channel_select.json"));
//        String blocksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode.get("blocks"));
//
//        // Step 2: Slack 메시지 전송
//        Slack slack = Slack.getInstance();
//        var client = slack.methods(botToken);
//
//        ChatPostMessageResponse response = client.chatPostMessage(req -> req
//                .channel(channelId)
//                .blocksAsString(blocksJson)
//                .text("Now, select a channel to receive notifications.")
//        );
//
//        if (!response.isOk()) {
//            System.err.println("Slack API Error: " + response.getError());
//        }
//    }
//}