

import com.merging.branchify.dto.CommitDto;
import com.merging.branchify.dto.CommitResponseDto;
import com.merging.branchify.entity.CommitEntity;
import com.merging.branchify.repository.CommitRepository;
import com.merging.branchify.service.GitService;
import com.merging.branchify.dto.CommitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GitServiceTest {


    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CommitRepository commitRepository;

    @InjectMocks
    private GitService gitService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCommits() {
        // 준비: Mock 커밋 데이터를 생성
        CommitDto[] commitDtos = new CommitDto[1];
        CommitDto.Commit.Author author = new CommitDto.Commit.Author();
        author.setName("Test Author");
        author.setDate("2024-10-09T12:34:56Z");

        CommitDto.Commit commit = new CommitDto.Commit();
        commit.setMessage("Test commit message");
        //author.setAuthor(author);

        CommitDto commitDto = new CommitDto();
        commitDto.setSha("abc123def");
        commitDto.setCommit(commit);

        commitDtos[0] = commitDto;

        // RestTemplate의 mock 동작 정의
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(CommitDto[].class)
        )).thenReturn(ResponseEntity.ok(commitDtos));

        // 실행
        List<CommitResponseDto> commits = gitService.getCommits("test-repo", "fake-token");

        // 검증: 결과가 예상대로인지 확인
        assertEquals(1, commits.size());
        assertEquals("Test commit message", commits.get(0).getMessage());
        assertEquals("Test Author", commits.get(0).getAuthor());

        // 데이터베이스 저장 확인
        verify(commitRepository, times(1)).save(any(CommitEntity.class));
    }
}