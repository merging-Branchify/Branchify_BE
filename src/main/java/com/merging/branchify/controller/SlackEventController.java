package com.merging.branchify.controller;
import com.merging.branchify.service.NotionService;
import com.merging.branchify.service.JiraService;
import com.merging.branchify.service.SlackService;
import com.slack.api.bolt.App;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.model.event.AppHomeOpenedEvent;
import java.util.concurrent.ConcurrentHashMap;

import static com.slack.api.model.block.Blocks.asBlocks;


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
    public void handleAppHomeOpenedEvent(AppHomeOpenedEvent event, EventContext ctx) {
        String userId = event.getUser();

        // 온보딩 여부 확인 및 처리
        if (!slackService.hasSeenOnboarding(userId)) {
            String onboardingJson = slackService.loadOnboardingJson();
            slackService.publishHomeTab(userId, onboardingJson);
            slackService.setOnboarded(userId);
        }
        ctx.ack(); // 이벤트 처리 완료 응답
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
        app.blockAction("database_select-action", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();;
            String selectedDatabaseId = req.getPayload().getActions().get(0).getSelectedOption().getValue();
            // 선택한 데이터베이스를 저장
            notionService.saveDatabaseSelection(userId, selectedDatabaseId);
            // 알림 채널 선택 메시지 표시
            String channelId = req.getPayload().getChannel().getId();
            slackService.sendChannelSelectMessage(channelId, botToken);

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
        app.blockAction("project_select-action", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            String selectedProjectId = req.getPayload().getActions().get(0).getSelectedOption().getValue();

            // Jira 프로젝트 저장
            jiraService.saveProjectSelection(userId, selectedProjectId);
            // 알림 채널 선택 메시지 표시
            String channelId = req.getPayload().getChannel().getId();
            slackService.sendChannelSelectMessage(channelId, botToken);

            ctx.respond("You selected Jira project: " + selectedProjectId);
            return ctx.ack();
        });
        // 알림 채널 선택 핸들러
        app.blockAction("channel_select", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            String selectedChannelId = req.getPayload().getActions().get(0).getSelectedConversation();

            // 선택한 채널을 저장-> 이 기능 필요한가?
            ctx.respond("You selected channel: <#" + selectedChannelId + ">");
            return ctx.ack();
        });
    }


}

