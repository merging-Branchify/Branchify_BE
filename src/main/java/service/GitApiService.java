package service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class GitApiService {
    private static final String GITHUB_API = "https://api.github.com";
    private final RestTemplate restTemplate;

    public GitApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //http 요청 시 필요한 필요한 헤더 생성하는 메소드
    //나도 restTemplate처음써봄..
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        //GitHub API 버전 v3를 사용하여 데이터를 요청한다는 의미로 Accept 헤더를 설정
        headers.set("Accept", "github.v3+json");
        return headers;
    }

    // 주어진 URL에서 데이터를 가져오는 메소드
    private String fetchData(String url) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // GET 메소드를 사용해 지정된 URL로 요청을 보내고 응답을 받음
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    // 레포지토리 가져오기
    public String getRepositoryInfo(String owner, String repo) {
        String url = GITHUB_API + "/repos/" + owner + "/" + repo;
        return fetchData(url);
    }

    // 커밋 가져오기
    public String getCommits(String owner, String repo) {
        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/commits";
        return fetchData(url);
    }

    // 브랜치 가져오기
    public String getBranches(String owner, String repo) {
        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/branches";
        return fetchData(url);
    }
}
