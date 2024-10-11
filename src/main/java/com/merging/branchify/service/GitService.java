package com.merging.branchify.service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.merging.branchify.dto.CommitResponseDto;
import com.merging.branchify.dto.CommitDto;
import com.merging.branchify.entity.CommitEntity;
import com.merging.branchify.repository.CommitRepository;

@Service
public class GitService {

    private final CommitRepository commitRepository;

    public GitService(CommitRepository commitRepository) {
        this.commitRepository = commitRepository;
    }

    public List<CommitResponseDto> getCommits(String owner, String repo) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        String gitToken = "-";
        headers.add("Authorization", "Bearer " + gitToken);
        headers.add("Accept", "application/vnd.github.v3+json");

        // GitHub API에서 특정 저장소의 커밋 내역을 가져오는 엔드포인트
        String url = "http://api/github.com/repos/" + owner + "/" + repo + "/commits";

        HttpEntity<?> request = new HttpEntity<>(headers);

        // API 요청 보내기
        ResponseEntity<CommitDto[]> response = restTemplate.exchange(url, HttpMethod.GET, request, CommitDto[].class);
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + Arrays.toString(response.getBody()));
        // 응답 처리
        if (response.getStatusCode().is2xxSuccessful()) {
            CommitDto[] commits = response.getBody();
            List<CommitResponseDto> commitResponseDtos = processCommits(commits);

            if (commits != null) {
                for (CommitDto commit : commits) {
                    System.out.println("Commit SHA: " + commit.getSha());
                    System.out.println("Commit Message: " + commit.getCommit().getMessage());
                    System.out.println("Commit Author: " + commit.getCommit().getAuthor());
                    // 추가적으로 날짜 등 필요한 정보를 출력할 수 있습니다.
                }
            } else {
                System.out.println("No commits found.");
            }


            // 커밋 정보를 데이터베이스에 저장
            for (CommitResponseDto commit : commitResponseDtos) {
                CommitEntity entity = new CommitEntity();
                entity.setSha(commit.getSha());
                entity.setMessage(commit.getMessage());
                entity.setAuthor(commit.getAuthor());
                entity.setDate(commit.getDate());
                commitRepository.save(entity); // DB에 저장
            }
            return commitResponseDtos;
        } else {
            System.out.println("Error: " + response.getStatusCode());
            return new ArrayList<>(); // 빈 리스트 반환
        }
    }

    // 커밋 데이터 가공
    private List<CommitResponseDto> processCommits(CommitDto[] commits) {
        List<CommitResponseDto> result = new ArrayList<>();
        //확인
//        if (commits == null) { return result; }

        for (CommitDto commit : commits) {
            CommitResponseDto dto = new CommitResponseDto();
            dto.setSha(commit.getSha());
            dto.setMessage(commit.getCommit().getMessage());
            dto.setAuthor(commit.getCommit().getAuthor());
            result.add(dto);
        }
        return result;
    }

}

