package com.merging.branchify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.merging.branchify.service.GitService;
import com.merging.branchify.dto.ResponseDto;
import com.merging.branchify.dto.CommitResponseDto;
import com.merging.branchify.user.User;



@RestController
@RequestMapping("/api.github.com")
public class GitController {
    @Autowired
    private GitService gitService;

    public GitController(GitService gitService) {
        this.gitService = gitService;
    }

    /**
     * 특정 저장소의 전체 커밋 내역을 조회하는 메서드
     * @return ResponseDto에 커밋 내역을 담아 반환
     */
    // 특정 저장소의 전체 커밋 내역 가져오기
    @GetMapping("/repos/{owner}/{repo}/commits")
    public ResponseEntity<ResponseDto<?>> getCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            //인증된 사용자
            @AuthenticationPrincipal User user) {
        String gitToken = user.getGitToken();
        // 후에 String gitToken = "user-token"; 으로 수정해야함 ->ㄴ

        //커밋 내역 조회, 이후에 user을 owner로 바꿔야함
        List<CommitResponseDto> commitResponse = gitService.getCommits(owner, repo);
        // 조회된 커밋 내역 성공 응답으로 반환
        return ResponseEntity.ok(ResponseDto.success(commitResponse));
    }

}
