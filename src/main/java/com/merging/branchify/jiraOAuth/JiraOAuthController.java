//package com.merging.branchify.jiraOAuth;
//
//import com.merging.branchify.slackBot.JiraProjectDTO;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/oauth/jira")
//public class JiraOAuthController {
//
//    private final JiraOAuthService jiraOAuthService;
//
//    public JiraOAuthController(JiraOAuthService jiraOAuthService) {
//        this.jiraOAuthService = jiraOAuthService;
//    }
//
//    // Jira OAuth 인증 시작
//    @GetMapping("/authorize")
//    public ResponseEntity<String> authorizeJira(@RequestParam String slackWorkspaceId) {
//        String authUrl = jiraOAuthService.generateAuthorizationUrl(slackWorkspaceId);
//        return ResponseEntity.ok(authUrl);
//    }
//
//    // Jira Callback 처리
//    @GetMapping("/callback")
//    public ResponseEntity<String> handleCallback(@RequestParam String code, @RequestParam String state) {
//        String accessToken = jiraOAuthService.exchangeAuthorizationCodeForAccessToken(code);
//        return ResponseEntity.ok("OAuth completed. Access Token: " + accessToken);
//    }
//
//    // Jira 프로젝트 목록 반환
//    @GetMapping("/projects")
//    public ResponseEntity<List<JiraProjectDTO>> getProjects(
//            @RequestParam String slackWorkspaceId,
//            @RequestHeader("Authorization") String authorization) {
//        String accessToken = authorization.replace("Bearer ", "");
//        List<JiraProjectDTO> projects = jiraOAuthService.getWorkspaceJiraProjects(slackWorkspaceId, accessToken);
//        return ResponseEntity.ok(projects);
//    }
//
//    // Jira 프로젝트 정보 저장
//    @PostMapping("/projects")
//    public ResponseEntity<String> saveProject(
//            @RequestHeader(value = "Authorization", required = false) String authorization,
//            @RequestBody JiraOAuthRequestDTO request) {
//
//        // Authorization 헤더에서 Slack 토큰 추출
//        String slackToken = authorization.replace("Bearer ", "");
//
//        // DB에서 workspaceId 조회
//        String workspaceId = jiraOAuthService.getWorkspaceIdBySlackToken(slackToken);
//
//        // 요청 데이터에 workspaceId 설정
//        request.setWorkspaceId(workspaceId);
//
//        // Jira 프로젝트 정보 저장
//        jiraOAuthService.saveJiraOAuth(request);
//        return ResponseEntity.ok("Jira project linked successfully");
//    }
//}
