package com.merging.branchify.slackOAuth;

import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/oauth")
public class SlackOAuthController {

    private final SlackOAuthService slackOAuthService;

    public SlackOAuthController(SlackOAuthService slackOAuthService) {
        this.slackOAuthService = slackOAuthService;
    }

    @Value("${slack.redirect.uri}")
    private String slackRedirectUri;

    // 플러그인 설치 시 호출되는 메서드
    @GetMapping("/slack/connect")
    public ResponseEntity<Void> redirectToSlackAuthPage() {
        String slackAuthUrl = "https://slack.com/oauth/v2/authorize" +
                "?client_id=" + "7867953200945.8055388782901" +
                "&scope=app_mentions:read,bookmarks:read,calls:write,channels:history,chat:write,commands,emoji:read,groups:history,im:history,metadata.message:read,mpim:history,reactions:read" +
                "&redirect_uri=" + slackRedirectUri;

        return ResponseEntity.status(302).header("Location", slackAuthUrl).build();
    }

    // Slack OAuth Callback 처리
    @GetMapping("/slack/callback")
    public ResponseEntity<String> handleSlackCallback(@RequestParam("code") String code) {
        try {
            // Authorization Code로 Access Token 교환
            OAuthV2AccessResponse response = slackOAuthService.exchangeSlackToken(code);

            if (!response.isOk()) {
                throw new RuntimeException("Slack API error: " + response.getError());
            }

            // 사용자 정보 저장
            SlackUserDTO slackUserDTO = slackOAuthService.registerOrUpdateSlackUser(response);

            // 사용자의 워크스페이스로 리다이렉트
            String redirectUrl = "slack://open?team=" + slackUserDTO.getWorkspaceId();

            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (IOException | SlackApiException e) {
            return ResponseEntity.status(500).body("Error during Slack OAuth callback: " + e.getMessage());
        }
    }
}
