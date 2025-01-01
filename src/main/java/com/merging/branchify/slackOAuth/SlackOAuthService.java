package com.merging.branchify.slackOAuth;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.oauth.OAuthV2AccessRequest;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SlackOAuthService {

    @Value("${slack.client.secret}")
    private String clientSecret;

    @Value("${slack.redirect.uri}")
    private String redirectUrl;

    private final SlackOAuthRepository slackUserRepository;

    public SlackOAuthService(SlackOAuthRepository slackUserRepository) {
        this.slackUserRepository = slackUserRepository;
    }

    // Slack OAuth Token 교환
    public OAuthV2AccessResponse exchangeSlackToken(String code) throws IOException, SlackApiException {
        MethodsClient methods = Slack.getInstance().methods();
        return methods.oauthV2Access(OAuthV2AccessRequest.builder()
                .clientId("7867953200945.8055388782901")
                .clientSecret(clientSecret)
                .code(code)
                .redirectUri(redirectUrl)
                .build());
    }

    // Slack 사용자 등록 또는 업데이트
    public SlackOAuthDTO registerOrUpdateSlackUser(OAuthV2AccessResponse response) {
        SlackOAuth slackUser = slackUserRepository.findByWorkspaceId(response.getTeam().getId());
        if (slackUser == null) {
            slackUser = new SlackOAuth();
            slackUser.setWorkspaceId(response.getTeam().getId());
            slackUser.setWorkspaceName(response.getTeam().getName());
        }

        slackUser.setAccessToken(response.getAccessToken());
        slackUserRepository.save(slackUser);

        // SlackUser 엔티티를 DTO로 변환하여 반환
        SlackOAuthDTO dto = new SlackOAuthDTO();
        dto.setWorkspaceId(slackUser.getWorkspaceId());
        dto.setWorkspaceName(slackUser.getWorkspaceName());
        dto.setAccessToken(slackUser.getAccessToken());
        return dto;
    }
}
