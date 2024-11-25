package com.merging.branchify.controller;

import com.google.gson.JsonObject;
import com.merging.branchify.service.NotionService;
import com.merging.branchify.service.JiraService;
import com.merging.branchify.service.SlackService;
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

//package com.merging.branchify.controller;
//import com.merging.branchify.service.NotionService;
//import com.merging.branchify.service.JiraService;
//import com.merging.branchify.service.SlackService;
//import com.slack.api.bolt.App;
//import org.apache.catalina.connector.Response;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import com.slack.api.bolt.context.builtin.EventContext;
//import com.slack.api.model.event.AppHomeOpenedEvent;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static com.slack.api.model.block.Blocks.asBlocks;
//
//
//@Component
//public class SlackEventController {
//
//    private final SlackService slackService;
//    private final NotionService notionService;
//    private final JiraService jiraService;
//
//    public SlackEventController(SlackService slackService, NotionService notionService, JiraService jiraService) {
//        this.slackService = slackService;
//        this.notionService = notionService;
//        this.jiraService = jiraService;
//    }
//
//    public void handleAppHomeOpenedEvent(AppHomeOpenedEvent event, EventContext context) {
//        // 사용자 ID 가져오기
//        String userId = event.getUser();
//
//        // SlackService에서 처리
//        slackService.handleAppHomeOpened(userId);
//
//        // ACK 응답 반환
//        context.ack();
//    }
//
//    @Value("${slack.bot.token}")
//    private String botToken;
//
//    public void configureSlackCommands(App app) {
//        // /notion_fy 명령어 핸들러
//        app.command("/notion_fy", (req, ctx) -> {
//            try {
//                // SlackService를 통해 JSON 업데이트 및 메시지 전송
//                String channelId = req.getPayload().getChannelId();
//                slackService.updateAndSendNotionMessage(channelId, botToken);
//                return ctx.ack();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return ctx.ack("Failed to send Notion database message.");
//            }
//        });
//
//        // Notion 데이터베이스 선택 핸들러
//        app.blockAction("noton_database_select-action", (req, ctx) -> {
//            String userId = req.getPayload().getUser().getId();;
//            String selectedDatabaseId = req.getPayload().getActions().get(0).getSelectedOption().getValue();
//            // 선택한 데이터베이스를 저장
//            notionService.saveDatabaseSelection(userId, selectedDatabaseId);
//            // 알림 채널 선택 메시지 표시
//            String channelId = req.getPayload().getChannel().getId();
//            slackService.sendChannelSelectMessage(channelId, botToken);
//
//
//            ///ctx.respond("You selected database: " + selectedDatabaseId);
//            return ctx.ack();
//        });
//
//        // /jira_fy 명령어 핸들러
//        app.command("/jira_fy", (req, ctx) -> {
//            try {
//                String channelId = req.getPayload().getChannelId();
//                slackService.updateAndSendJiraMessage(channelId, botToken);
//                return ctx.ack();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return ctx.ack("Failed to send Jira projects message.");
//            }
//        });
//
//        // Jira 프로젝트 선택 핸들러
//        app.blockAction("jira_project_select-action", (req, ctx) -> {
//            String userId = req.getPayload().getUser().getId();
//            String selectedProjectId = req.getPayload().getActions().get(0).getSelectedOption().getValue();
//
//            // Jira 프로젝트 저장
//            jiraService.saveProjectSelection(userId, selectedProjectId);
//            // 알림 채널 선택 메시지 표시
//            String channelId = req.getPayload().getChannel().getId();
//            slackService.sendChannelSelectMessage(channelId, botToken);
//
//            //ctx.respond("You selected Jira project: " + selectedProjectId);
//            return ctx.ack();
//        });
//        // 알림 채널 선택 핸들러
//        app.blockAction("channel_select", (req, ctx) -> {
//            String userId = req.getPayload().getUser().getId();
//            String selectedChannelId = req.getPayload().getActions().get(0).getSelectedConversation();
//
//            slackService.saveSelectedChannel(userId, selectedChannelId);
//            //ctx.respond("You selected channel: <#" + selectedChannelId + ">");
//            return ctx.ack();
//        });
//    }
//
//
//}
//
