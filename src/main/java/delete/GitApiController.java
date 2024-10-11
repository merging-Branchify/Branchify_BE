package delete;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GitApiController {
    @Autowired
    private GitApiService gitApiService;

    @GetMapping("/repos/{owner}/{repo}")
    public String getRepositoryInfo(@PathVariable String owner, @PathVariable String repo, @PathVariable String token) {
        return gitApiService.getRepositoryInfo(owner, repo, token);
    }

    @GetMapping("/repos/{owner}/{repo}/commits/{commits_id}")
    public String getCommits(@PathVariable String owner, @PathVariable String repo, @PathVariable String token) {
        return gitApiService.getCommits(owner, repo, token);
    }

    @GetMapping("/repos/{owner}/{repo}/branches")
    public String getBranches(@PathVariable String owner, @PathVariable String repo, @PathVariable String token) {
        return gitApiService.getBranches(owner, repo, token);
    }
    @GetMapping("/repos/{owner}/{repo}/commits/{branch}")
    public String getBranchCommits(@PathVariable String owner, @PathVariable String repo,
                                  @PathVariable String branch, @PathVariable String token) {
        return gitApiService.getBranchCommits(owner, repo, branch, token);
    }

}
