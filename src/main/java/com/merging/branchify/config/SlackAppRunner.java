package com.merging.branchify.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.event.AppHomeOpenedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.merging.branchify.service.SlackService;
import com.merging.branchify.controller.SlackEventController;

@Component
public class SlackAppRunner implements CommandLineRunner {

    private final String appToken;
    private final SlackService slackService;
    private final SlackEventController slackEventController;

    public SlackAppRunner(@Value("${slack.app.token}") String appToken, SlackService slackService, SlackEventController slackEventController) {
        this.appToken = appToken;
        this.slackService = slackService;
        this.slackEventController = slackEventController;
    }
    @Override
    public void run(String... args) {
        try {
            System.out.println("Initializing Slack Bolt App...");

            // Slack Bolt 앱 초기화
            App app = new App();

            app.use((req, res, chain) -> {
                System.out.println("Received request: " + req.getRequestBodyAsString());
                return chain.next(req);
            });

            // App Home Opened 이벤트 핸들러 등록
            app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
                //System.out.println("App Home Opened Event Triggered by User: " + payload.getEvent().getUser());
                String userId = payload.getEvent().getUser();
                System.out.println("App Home Opened Event Triggered by User: " + userId);

                slackService.handleAppHomeOpened(userId);
                return ctx.ack();
            });

            slackEventController.configureSlackCommands(app); // 핸들러 등록

            // WebSocket 연결 시작
            System.out.println("Starting WebSocket connection...");
            SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
            socketModeApp.start();
            System.out.println("WebSocket connection established.");

        } catch (Exception e) {
            System.err.println("Error during Slack WebSocket initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

