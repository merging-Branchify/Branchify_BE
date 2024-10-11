package com.merging.branchify.service;

import com.merging.branchify.dto.CommitDTO;
import com.merging.branchify.entity.Commit;
import com.merging.branchify.respository.CommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitService {

    //  application.yml 파일에 정의된 GitHub API 토큰을 읽어와 gitHubToken 변수에 할당
    @Value("${github.token}")
    private String githubToken;

    private final String BASE_URL = "https://api.github.com/repos";
    private final CommitRepository commitRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // GitHub API에서 특정 레포지토리의 커밋 데이터를 가져옴
    public List<Commit> getCommitsFromApi(String owner, String repo) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .pathSegment(owner, repo, "commits")
                .toUriString(); // URL 생성

        // 인증 토큰을 헤더에 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer" + githubToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //Github API에  GET 요청을 보냄
        ResponseEntity<CommitDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, CommitDTO[].class);

        if (response.getBody() == null) {
            return List.of(); // 응답 본문이 없을 경우 빈 리스트 반환
        }

        return saveCommits(Arrays.asList(response.getBody()), repo);
    }

    // GitHub API로부터 가져온 커밋 데이터를 엔티티로 변환하여 데이터베이스에 저장함
    private List<Commit> saveCommits(List<CommitDTO> commits, String repoName) {
        List<Commit> commitEntities = commits.stream().map(commit ->
                new Commit(
                        commit.getSha(),
                        commit.getCommit().getAuthor().getName(),
                        commit.getCommit().getAuthor().getEmail(),
                        commit.getCommit().getMessage(),
                        commit.getCommit().getAuthor().getDate(),
                        repoName
                )
        ).toList();

        return commitRepository.saveAll(commitEntities);
    }
}
