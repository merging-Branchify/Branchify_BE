package com.merging.branchify.controller;
import com.google.gson.JsonObject;
import com.merging.branchify.service.NotionService;
import com.merging.branchify.service.JiraService;
import com.merging.branchify.service.SlackService;
import com.slack.api.bolt.App;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SlackEventController {

    private final SlackService slackService;
    private final NotionService notionService;
    private final JiraService jiraService;

    public SlackEventController(SlackService slackService, NotionService notionService, JiraService jiraService) {
        this.slackService = slackService;
        this.notionService = notionService;
        this.jiraService = jiraService;
    }

    @Value("${slack.bot.token}")
    private String botToken;

    public void configureSlackCommands(App app) {
        // /notion_fy 명령어 핸들러
        app.command("/notion_fy", (req, ctx) -> {
            try {
                // SlackService를 통해 JSON 업데이트 및 메시지 전송
                String channelId = req.getPayload().getChannelId();
                slackService.updateAndSendNotionMessage(channelId, botToken);
                return ctx.ack();
            } catch (Exception e) {
                e.printStackTrace();
                return ctx.ack("Failed to send Notion database message.");
            }
        });

        // Notion 데이터베이스 선택 핸들러
        app.blockAction("notion_database_select", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();;
            String selectedDatabaseId = req.getPayload().getActions().get(0).getSelectedOption().getValue();
            // 선택한 데이터베이스를 저장 (데이터베이스 로직 필요)
            notionService.saveDatabaseSelection(userId, selectedDatabaseId);

            ctx.respond("You selected database: " + selectedDatabaseId);
            return ctx.ack();
        });

        // /jira_fy 명령어 핸들러
        app.command("/jira_fy", (req, ctx) -> {
            try {
                String channelId = req.getPayload().getChannelId();
                slackService.updateAndSendJiraMessage(channelId, botToken);
                return ctx.ack();
            } catch (Exception e) {
                e.printStackTrace();
                return ctx.ack("Failed to send Jira projects message.");
            }
        });

        // Jira 프로젝트 선택 핸들러
        app.blockAction("jira_project_select", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            String selectedProjectId = req.getPayload().getActions().get(0).getSelectedOption().getValue();

            // Jira 프로젝트 저장
            jiraService.saveProjectSelection(userId, selectedProjectId);
            ctx.respond("You selected Jira project: " + selectedProjectId);
            return ctx.ack();
        });
        // 알림 채널 선택 핸들러
        app.blockAction("selected_channel", (req, ctx) -> {
            try {
                String userId = req.getPayload().getUser().getId();
                String selectedChannelId = req.getPayload().getActions().get(0).getSelectedConversation();

                // 선택한 채널을 저장-> 이 기능 필요한가?
                ctx.respond("You selected channel: <#" + selectedChannelId + ">");
                return ctx.ack();
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("response_type", "ephemeral");
                errorResponse.addProperty("text", "Failed to process channel selection.");

                return ctx.ack(errorResponse);
            }
        });
    }


}




// /jira_connect 명령어 핸들러
//        app.command("/jira_connect", (req, ctx) -> {
//            List<Option> options = slackService.getJiraProjectOptions(); // 지라 프로젝트 데이터를 가져옵니다.
//            List<LayoutBlock> blocks = slackService.createJiraProjectBlock(options); // 지라 프로젝트 블록 생성.
//            ctx.respond(res -> res.blocks(blocks));
//            return ctx.ack();
//        });