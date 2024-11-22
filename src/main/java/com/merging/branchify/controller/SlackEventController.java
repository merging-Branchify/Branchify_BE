package com.merging.branchify.controller;
import com.merging.branchify.service.SlackService;
import com.slack.api.bolt.App;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlackEventController {

    private final SlackService slackService;

    public SlackEventController(SlackService slackService) {
        this.slackService = slackService;
    }

    public void configureSlackCommands(App app) {
        // /notion_connect 명령어 핸들러
        app.command("/notion_connect", (req, ctx) -> {
            try {
                // SlackService를 통해 JSON 업데이트 및 메시지 전송
                String channelId = req.getPayload().getChannelId();
                String botToken = System.getenv("SLACK_BOT_TOKEN"); // 환경변수에서 Bot Token 가져오기
                slackService.updateAndSendMessage(channelId, botToken);
                return ctx.ack();
            } catch (Exception e) {
                e.printStackTrace();
                return ctx.ack("Failed to send Notion database message.");
            }
        });

        // Notion 데이터베이스 선택 핸들러
        app.blockAction("notion_database_select", (req, ctx) -> {
            String selectedDatabaseId = req.getPayload().getActions().get(0).getSelectedOption().getValue();
            ctx.respond("You selected database: " + selectedDatabaseId);
            return ctx.ack();
        });

        // 알림 채널 선택 핸들러
        app.blockAction("notification_channel_select", (req, ctx) -> {
            String selectedChannel = req.getPayload().getActions().get(0).getSelectedOption().getValue();
            ctx.respond("Notifications will be sent to channel: " + selectedChannel);
            return ctx.ack();
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