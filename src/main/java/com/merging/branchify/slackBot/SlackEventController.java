package com.merging.branchify.slackBot;

import com.google.gson.JsonObject;
import com.merging.branchify.jiraIssue.JiraService;
import com.merging.branchify.notionDatabase.NotionService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AppHomeOpenedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.slack.api.bolt.context.builtin.EventContext;


@Component
public class SlackEventController {

    private final SlackService slackService;
    private final NotionService notionService;
    private final JiraService jiraService;

    @Value("${slack.bot.token}")
    private String botToken;

    public SlackEventController(SlackService slackService, NotionService notionService, JiraService jiraService) {
        this.slackService = slackService;
        this.notionService = notionService;
        this.jiraService = jiraService;
    }

    public void handleAppHomeOpenedEvent(AppHomeOpenedEvent event, EventContext context) {
        // 사용자 ID 가져오기
        String userId = event.getUser();

        // SlackService에서 처리
        slackService.handleAppHomeOpened(userId);

        // ACK 응답 반환
        context.ack();
    }

    public void configureSlackCommands(App app) {
        // /notion_fy 슬래시 커맨드
        app.command("/notion_fy", (req, ctx) -> {

            try {
                String channelId = req.getPayload().getChannelId();
                var databases = notionService.listDatabases();
                slackService.sendNotionDatabaseBlock(channelId, databases);

                JsonObject response = new JsonObject();
                response.addProperty("text", "Notion database selection sent.");
                return ctx.ack(response);
            } catch (Exception e) {
                e.printStackTrace();

                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("text", "Failed to process Notion command.");
                return ctx.ack(errorResponse);
            }
        });

        // /jira_fy 슬래시 커맨드
        app.command("/jira_fy", (req, ctx) -> {
            try {
                String channelId = req.getPayload().getChannelId();
                var projects = jiraService.fetchProjectList();
                slackService.sendJiraProjectBlock(channelId, projects);

                JsonObject response = new JsonObject();
                response.addProperty("text", "Jira project selection sent.");
                return ctx.ack(response);
            } catch (Exception e) {
                e.printStackTrace();

                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("text", "Failed to process Jira command.");
                return ctx.ack(errorResponse);
            }
        });

        // Notion 또는 Jira 선택 후 채널 선택 블록 표시
        app.blockAction("dynamic_select_action", (req, ctx) -> {
            try {
                String userId = req.getPayload().getUser().getId();
                String selectedId = req.getPayload().getActions().get(0).getSelectedOption().getValue();

                if (req.getPayload().getMessage().getText().contains("Notion")) {
                    notionService.saveDatabaseSelection(userId, selectedId);
                } else if (req.getPayload().getMessage().getText().contains("Jira")) {
                    jiraService.saveProjectSelection(userId, selectedId);
                }

                String channelId = req.getPayload().getChannel().getId();
                slackService.sendChannelSelectBlock(channelId);

                JsonObject response = new JsonObject();
                response.addProperty("text", "Selection processed.");
                return ctx.ack(response);
            } catch (Exception e) {
                e.printStackTrace();

                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("text", "Failed to process selection.");
                return ctx.ack(errorResponse);
            }
        });

        // 채널 선택 후 저장
        app.blockAction("channel_select_action", (req, ctx) -> {
            try {
                String userId = req.getPayload().getUser().getId();
                String selectedChannelId = req.getPayload().getActions().get(0).getSelectedConversation();
                slackService.saveSelectedChannel(userId, selectedChannelId);

                JsonObject response = new JsonObject();
                response.addProperty("text", "Channel selection saved.");
                return ctx.ack(response);
            } catch (Exception e) {
                e.printStackTrace();

                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("text", "Failed to save channel selection.");
                return ctx.ack(errorResponse);
            }
        });
    }
}
