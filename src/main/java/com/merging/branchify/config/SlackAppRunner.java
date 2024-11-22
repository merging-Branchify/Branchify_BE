package com.merging.branchify.config;

import com.merging.branchify.controller.SlackEventController;
import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component // Spring Boot에서 이 클래스를 Bean으로 등록
public class SlackAppRunner implements CommandLineRunner {

    private final SlackEventController slackEventController;
    private final String appToken;

    public SlackAppRunner(SlackEventController slackEventController,
                          @Value("${slack.app.token}") String appToken) {
        this.slackEventController = slackEventController;
        this.appToken = appToken;
    }

    @Override
    public void run(String... args) throws Exception {
        // Slack Bolt App 설정 및 실행
        App app = new App();
        slackEventController.configureSlackCommands(app); // 명령어 및 핸들러 등록
        SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
        socketModeApp.start(); // Slack 앱 실행
    }
}