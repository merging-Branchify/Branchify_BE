package com.merging.branchify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private static final String JSON_FILE_PATH = "src/main/resources/database_select.json"; // JSON 파일 경로
    private final ObjectMapper mapper = new ObjectMapper();

    public SlackService(NotionService notionService) {
        this.notionService = notionService;
    }

    //Notion 데이터를 JSON 파일에 추가 (드롭다운 옵션 업데이트)//
    public void updateJsonWithDatabases() throws IOException {
        List<NotionDatabaseDTO> databases = notionService.listDatabases();

        // JSON 파일 읽기
        JsonNode rootNode = mapper.readTree(new File(JSON_FILE_PATH));
        JsonNode blocks = rootNode.get("blocks");
        JsonNode accessory = blocks.get(0).get("accessory");

        // 기존 옵션 대체
        List<JsonNode> options = databases.stream().map(database -> {
            JsonNode option = mapper.createObjectNode();
            ((ObjectNode) option).putObject("text")
                    .put("type", "plain_text")
                    .put("text", database.getTitle());
            ((ObjectNode) option).put("value", database.getId());
            return option;
        }).toList();

        ((ObjectNode) accessory).set("options", mapper.createArrayNode().addAll(options));

        // JSON 파일 저장
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_FILE_PATH), rootNode);
        System.out.println("Updated JSON: " + rootNode);
    }

    //Slack 메시지 전송//
    public void sendBlockMessage(String channelId, String botToken) throws IOException, SlackApiException {
        // JSON 파일 읽기
        JsonNode rootNode = mapper.readTree(new File(JSON_FILE_PATH));
        String blocksJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode.get("blocks"));

        // Slack API 호출
        Slack slack = Slack.getInstance();
        var client = slack.methods(botToken);

        ChatPostMessageResponse response = client.chatPostMessage(req -> req
                .channel(channelId)
                .blocksAsString(blocksJson) // JSON 블록 전송
        );

        if (!response.isOk()) {
            System.err.println("Slack API Error: " + response.getError());
        } else {
            System.out.println("Slack Message Sent Successfully!");
        }
    }
    public void updateAndSendMessage(String channelId, String botToken) throws IOException, SlackApiException {
        // JSON 파일 업데이트
        updateJsonWithDatabases();

        // Slack 메시지 전송
        sendBlockMessage(channelId, botToken);
    }
}

//    //Jira 데이터를 JSON 파일에 추가 (드롭다운 옵션 업데이트)
//    public void updateJsonWithJiraProjects() throws IOException {
//        // Jira 프로젝트 데이터를 가져옴
//        List<Map<String, String>> jiraProjects = jiraService.getJiraProjects();
//
//        // JSON 파일 읽기
//        JsonNode rootNode = mapper.readTree(new File(JSON_FILE_PATH));
//        JsonNode blocks = rootNode.get("blocks");
//        JsonNode accessory = blocks.get(0).get("accessory");
//
//        // 기존 옵션 대체
//        List<JsonNode> options = jiraProjects.stream().map(project -> {
//            JsonNode option = mapper.createObjectNode();
//            ((ObjectNode) option).putObject("text")
//                    .put("type", "plain_text")
//                    .put("text", project.get("name")); // 프로젝트 이름
//            ((ObjectNode) option).put("value", project.get("id")); // 프로젝트 ID
//            return option;
//        }).toList();
//
//        ((ObjectNode) accessory).set("options", mapper.createArrayNode().addAll(options));
//
//        // JSON 파일 저장
//        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_FILE_PATH), rootNode);
//        System.out.println("Updated JSON for Jira Projects: " + rootNode);
//    }