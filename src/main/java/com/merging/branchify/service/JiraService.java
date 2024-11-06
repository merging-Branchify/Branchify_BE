package com.merging.branchify.service;

import com.merging.branchify.dto.JiraIssueDTO;
import com.merging.branchify.entity.JiraIssue;
import com.merging.branchify.respository.JiraIssueRepository;
import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class JiraService {

    // application.yml에서 설정 값을 불러옴
    @Value("${jira.api.base-url}")
    private String jiraBaseUrl;

    @Value("${jira.api.username}")
    private String jiraUsername;

    @Value("${jira.api.token}")
    private String jiraToken;

    private final JiraIssueRepository jiraIssueRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // REST API 요청을 위한 객체 생성

    // Repository를 생성자 주입
    public JiraService(JiraIssueRepository jiraIssueRepository) {
        this.jiraIssueRepository = jiraIssueRepository;
    }

    // Jira에서 하루 동안 변경된 이슈를 가져와 저장하는 메소드
    public void fetchAndStoreJiraUpdates() {
        // Jira API URL에 하루 동안 변경된 이슈를 가져오는 JQL 쿼리 추가
        String url = jiraBaseUrl + "/search?jql=updated >= -1d";

        // 인증 정보를 포함한 요청 헤더 생성
        HttpHeaders headers = createAuthHeaders();

        // 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // 응답이 성공적이라면 JSON 데이터를 파싱하여 저장
        if (response.getStatusCode().is2xxSuccessful()) {
            parseAndSaveIssues(response.getBody());
        } else {
            System.out.println("API 요청 실패 - 상태 코드: " + response.getStatusCode());
        }
    }

    // 인증 헤더 생성 메소드
    private HttpHeaders createAuthHeaders() {
        // 인증을 위한 Base64 인코딩 (username:token 형태)
        String auth = Base64.getEncoder().encodeToString((jiraUsername + ":" + jiraToken).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);

        return headers;
    }

    // 응답 JSON 데이터를 파싱하고, 각 이슈 정보를 데이터베이스에 저장
    private void parseAndSaveIssues(String responseBody) {
        // 응답 본문을 JSON 객체로 변환
        JSONArray issues = new JSONObject(responseBody).getJSONArray("issues");

        // 각 이슈를 순회하면서 데이터를 추출하고 저장
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JiraIssueDTO issueDTO = mapToDTO(issue); // JSON 데이터를 DTO로 매핑
            saveIssue(issueDTO); // DTO 데이터를 데이터베이스에 저장
        }
    }

    // JSON 데이터를 JiraIssueDTO 객체로 변환하는 메소드
    private JiraIssueDTO mapToDTO(JSONObject issue) {
        JiraIssueDTO issueDTO = new JiraIssueDTO();

        issueDTO.setIssueId(issue.getString("id"));
        issueDTO.setSummary(issue.getJSONObject("fields").getString("summary"));
        issueDTO.setStatus(issue.getJSONObject("fields").getJSONObject("status").getString("name"));

        // assignee(담당자)가 존재하고 JSON 객체일 경우에만 할당, 그렇지 않으면 "Unassigned"로 설정
        if (issue.getJSONObject("fields").has("assignee") && !issue.getJSONObject("fields").isNull("assignee")) {
            JSONObject assignee = issue.getJSONObject("fields").getJSONObject("assignee");
            issueDTO.setAssignee(assignee.getString("displayName"));
        } else {
            issueDTO.setAssignee("Unassigned");
        }

        issueDTO.setProjectKey(issue.getJSONObject("fields").getJSONObject("project").getString("key"));

        // updatedAt 필드 파싱 (시간대 오프셋을 제거하고 파싱)
        String updatedString = issue.getJSONObject("fields").getString("updated");
        // +0900 부분을 제거하여 LocalDateTime으로 파싱 가능한 형식으로 변환
        if (updatedString.contains("+")) {
            updatedString = updatedString.substring(0, updatedString.indexOf('+'));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.parse(updatedString, formatter);
        issueDTO.setUpdatedAt(localDateTime);

        return issueDTO;
    }

    // DTO 데이터를 데이터베이스 엔티티로 변환하여 저장
    private void saveIssue(JiraIssueDTO issueDTO) {
        JiraIssue jiraIssue = new JiraIssue();

        jiraIssue.setIssueId(issueDTO.getIssueId());
        jiraIssue.setSummary(issueDTO.getSummary());
        jiraIssue.setStatus(issueDTO.getStatus());
        jiraIssue.setAssignee(issueDTO.getAssignee());
        jiraIssue.setUpdatedAt(issueDTO.getUpdatedAt());
        jiraIssue.setProjectKey(issueDTO.getProjectKey());

        try {
            jiraIssueRepository.save(jiraIssue);
        } catch (DataIntegrityViolationException e) {
            System.out.println("중복된 issueId가 존재합니다: " + jiraIssue.getIssueId());
        }
    }
}