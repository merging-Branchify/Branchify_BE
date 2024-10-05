package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import service.GitApiService;

@RestController
public class GitApiController {
    @Autowired
    private GitApiService gitApiService;

    @GetMapping("/repos/{owner}/{repo}")
    public String getRepositoryInfo(@PathVariable String owner, @PathVariable String repo) {
        return gitApiService.getRepositoryInfo(owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/commits")
    public String getCommits(@PathVariable String owner, @PathVariable String repo) {
        return gitApiService.getCommits(owner, repo);
    }

    @GetMapping("/repos/{owner}/{repo}/branches")
    public String getBranches(@PathVariable String owner, @PathVariable String repo) {
        return gitApiService.getBranches(owner, repo);
    }

}
