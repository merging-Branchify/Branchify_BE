package com.merging.branchify.slackBot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.merging.branchify.notionDatabase.NotionDatabaseDTO;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.views.ViewsPublishResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

@Service
public class SlackService {
    private final ChannelRepository channelRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${slack.bot.token}")
    private String botToken;

    public SlackService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public void handleAppHomeOpened(String userId) {
        // 온보딩 JSON 로드
        String onboardingBlocks = loadOnboardingBlocks();

        // 홈 탭 업데이트 API 호출
        Slack slack = Slack.getInstance();
        try {
            // views.publish 호출
            ViewsPublishResponse response = slack.methods(botToken).viewsPublish(r -> r
                    .userId(userId)
                    .viewAsString(onboardingBlocks)  // viewAsString으로 JSON 문자열 전달
            );
            if (response.isOk()) {
                System.out.println("App Home updated successfully!");
            } else {
                System.err.println("Error updating App Home: " + response.getError());
                // 오류에 대한 자세한 응답 내용 -> 추후 삭제
                System.err.println("Error details: " + response.getResponseMetadata());
            }
     } catch (IOException | SlackApiException e) {
            System.err.println("Error updating App Home: " + e.getMessage());
            e.printStackTrace();
     }
    }


    private String loadOnboardingBlocks() {
        // onboarding.json 파일을 리소스에서 읽기
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("onboarding.json")) {
            if (inputStream == null) {
                System.err.println("onboarding.json file not found in resources.");
                return "{}"; // 기본값
            }
            // 파일을 String으로 변환하여 반환
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                return scanner.useDelimiter("\\A").next();  // 파일 전체를 한 번에 읽기
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "{}"; // 기본값
        }
    }

    public void sendNotionDatabaseBlock(String channelId, List<NotionDatabaseDTO> databases) throws IOException, SlackApiException {
        // 옵션 리스트 생성
        ArrayNode options = mapper.createArrayNode();
        for (NotionDatabaseDTO db : databases) {
            options.add(createOption(db.getTitle(), db.getId()));
        }

        // 블록 생성
        String blocksJson = createStaticSelectBlock("🔗연동할 Notion 프로젝트를 선택하세요.", options, "dynamic_select_action");

        // Slack 메시지 전송
        sendMessage(channelId, blocksJson, "Notion database selection");
    }

    public void sendJiraProjectBlock(String channelId, List<JiraProjectDTO> projects) throws IOException, SlackApiException {
        // 옵션 리스트 생성
        ArrayNode options = mapper.createArrayNode();
        for (JiraProjectDTO proj : projects) {
            options.add(createOption(proj.getProjectName(), proj.getProjectId()));
        }

        // 블록 생성
        String blocksJson = createStaticSelectBlock("🔗연동할 Jira 프로젝트를 선택하세요.", options, "dynamic_select_action");

        // Slack 메시지 전송
        sendMessage(channelId, blocksJson, "Jira project selection");
    }

    public void sendChannelSelectBlock(String channelId) throws IOException, SlackApiException {
        // Conversations Select 블록 생성
        String blocksJson = createChannelSelectBlock("🔔 Select a Slack channel for notifications:", "channel_select_action");

        // Slack 메시지 전송
        sendMessage(channelId, blocksJson, "Channel selection prompt");
    }

    private String createChannelSelectBlock(String label, String actionId) {
        ObjectNode block = mapper.createObjectNode();
        block.put("type", "section");

        // 텍스트 노드
        ObjectNode textNode = mapper.createObjectNode();
        textNode.put("type", "mrkdwn");
        textNode.put("text", label);
        block.set("text", textNode);

        // Conversations Select 액세서리
        ObjectNode accessory = mapper.createObjectNode();
        accessory.put("type", "conversations_select");

        ObjectNode placeholder = mapper.createObjectNode();
        placeholder.put("type", "plain_text");
        placeholder.put("text", "Select a channel");
        placeholder.put("emoji", true);

        accessory.set("placeholder", placeholder);
        accessory.put("action_id", actionId);

        block.set("accessory", accessory);

        // 블록 배열 생성
        ArrayNode blocks = mapper.createArrayNode();
        blocks.add(block);

        // JSON 문자열 반환
        return blocks.toString();
    }

    private ObjectNode createOption(String text, String value) {
        ObjectNode option = mapper.createObjectNode();
        ObjectNode textNode = mapper.createObjectNode();
        textNode.put("type", "plain_text");
        textNode.put("text", text);
        textNode.put("emoji", true);

        option.set("text", textNode);
        option.put("value", value);

        return option;
    }

    private String createStaticSelectBlock(String label, ArrayNode options, String actionId) throws IOException {
        ObjectNode block = mapper.createObjectNode();
        block.put("type", "section");

        ObjectNode textNode = mapper.createObjectNode();
        textNode.put("type", "mrkdwn");
        textNode.put("text", label);
        block.set("text", textNode);

        ObjectNode accessory = mapper.createObjectNode();
        accessory.put("type", "static_select");

        ObjectNode placeholder = mapper.createObjectNode();
        placeholder.put("type", "plain_text");
        placeholder.put("text", "Select an option");
        placeholder.put("emoji", true);

        accessory.set("placeholder", placeholder);
        accessory.set("options", options);
        accessory.put("action_id", actionId);

        block.set("accessory", accessory);

        ArrayNode blocks = mapper.createArrayNode();
        blocks.add(block);

        return blocks.toString();
    }

    private void sendMessage(String channelId, String blocksJson, String text) throws IOException, SlackApiException {
        Slack slack = Slack.getInstance();
        ChatPostMessageResponse response = slack.methods(botToken).chatPostMessage(req -> req
                .channel(channelId)
                .blocksAsString(blocksJson)
                .text(text));

        if (!response.isOk()) {
            // 에러 로깅
            System.err.println("Slack API Error: " + response.getError());
            if (response.getResponseMetadata() != null) {
                System.err.println("Response Metadata: " + response.getResponseMetadata().toString());
            }
            // 사용자 정의 예외 처리 (필요한 경우)
            throw new RuntimeException("Failed to send message to Slack: " + response.getError());
        }
    }

    public void saveSelectedChannel(String userId, String channelId) {
        ChannelSelection channelSelection = new ChannelSelection(userId, channelId);
        channelRepository.save(channelSelection);
    }
}