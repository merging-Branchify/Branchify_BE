package com.merging.branchify.controller;

import com.merging.branchify.entity.Commit;
import com.merging.branchify.service.CommitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommitController {

    private final CommitService commitService;

    @GetMapping("/api/commits/{owner}/{repo}")
    public List<Commit> getCommits(@PathVariable String owner,@PathVariable String repo) {
        return commitService.getCommitsFromApi(owner, repo);
    }
}
