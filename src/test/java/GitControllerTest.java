//
//import com.merging.branchify.dto.CommitResponseDto;
//import com.merging.branchify.dto.ResponseDto;
//import com.merging.branchify.service.GitService;
//import com.merging.branchify.controller.GitController;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//
//public class GitControllerTest {
//    @Mock
//    private GitService gitService;
//
//    @InjectMocks
//    private GitController gitController;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testGetCommits() {
//        // 준비: Mock 커밋 데이터를 생성
//        List<CommitResponseDto> mockCommits = new ArrayList<>();
//        CommitResponseDto commitResponseDto = new CommitResponseDto();
//        commitResponseDto.setSha("abc123def");
//        commitResponseDto.setMessage("Test commit message");
//        commitResponseDto.setAuthor("Test Author");
//        mockCommits.add(commitResponseDto);
//
//        // GitService의 mock 동작 정의
//        when(gitService.getCommits(anyString(), anyString())).thenReturn(mockCommits);
//
//        // 실행
//        ResponseEntity<ResponseDto<?>> response = gitController.getCommits("test-repo", null);
//
//        // 검증: 결과가 예상대로인지 확인
//        ResponseDto<?> responseBody = response.getBody();
//        assertEquals(200, responseBody.getStatus());
//        assertEquals("success", responseBody.getMessage());
//
//        List<CommitResponseDto> commits = (List<CommitResponseDto>) responseBody.getData();
//        assertEquals(1, commits.size());
//        assertEquals("Test commit message", commits.get(0).getMessage());
//
//        // GitService 호출 확인
//        verify(gitService, times(1)).getCommits(anyString(), anyString());
//    }
//}
