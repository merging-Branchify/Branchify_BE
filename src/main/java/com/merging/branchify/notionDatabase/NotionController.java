package com.merging.branchify.notionDatabase;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notion")
public class NotionController {

    private final NotionService notionService;

    public NotionController(NotionService notionService) {
        this.notionService = notionService;
    }

    // 선택된 데이터베이스 저장 (제목 기준)
    private final Map<String, NotionDatabaseDTO> selectedDatabase = new HashMap<>();

    // 데이터베이스별 마지막 조회 시각 저장
    private final Map<String, String> lastEditedTimes = new HashMap<>();

    // 데이터베이스 목록 조회
    @PostMapping("/databases")
    public List<NotionDatabaseDTO> listdatabases() {
        return notionService.listDatabases();
    }

    // 제목으로 데이터베이스 선택
    @PostMapping("/databases/select")
    public String selectDatabase(@RequestBody DatabaseSelectionRequest request) {
        Long userId = request.getUserId();
        String title = request.getTitle();

        if (title == null || title.isBlank()) {
            return "Invalid request: 'title' is required.";
        }

        // 데이터베이스 목록 가져오기
        List<NotionDatabaseDTO> databases = notionService.listDatabases();

        // 제목으로 데이터베이스 검색
        for (NotionDatabaseDTO database : databases) {
            if (database.getTitle().equals(title)) {
                // 사용자별 선택된 데이터베이스 저장
                selectedDatabase.put("default", database);
                return "Selected database: " + database.getTitle();
            }
        }
        return "Database with title '" + title + "' not found.";
    }

    // 선택된 데이터베이스 조회
    @GetMapping("/databases/selected")
    public NotionDatabaseDTO getSelectedDatabase() {
        NotionDatabaseDTO database = selectedDatabase.get("default");
        if (database == null) {
            throw new IllegalArgumentException("No database selected");
        }
        return database;
    }

    // 선택된 데이터베이스의 전체 내용 조회
    @GetMapping("/databases/contents")
    public List<Map<String, Object>> getDatabaseContent() {
        // 선택된 데이터베이스 가져오기
        NotionDatabaseDTO database = selectedDatabase.get("default");
        if (database == null) {
            throw new IllegalArgumentException("No database selected.");
        }

        String databaseId = database.getId();

        // 데이터베이스의 전체 내용 조회
        return notionService.getAllContents(databaseId);
    }

    // 선택된 데이터베이스의 수정된 내용 조회
    @GetMapping("/databases/changes")
    public List<Map<String, Object>> getDatabaseChanges() {
        // 선택된 데이터베이스 가져오기
        NotionDatabaseDTO database = selectedDatabase.get("default");
        if (database == null) {
            throw new IllegalArgumentException("No database selected.");
        }

        String databaseId = database.getId();

        // 마지막 조회 시각 가져오기
        String lastEditedTime = lastEditedTimes.getOrDefault(databaseId, "1970-01-01T00:00:00.000Z");

        // 변경된 데이터 조회
        List<Map<String, Object>> changes = notionService.getModifiedContents(databaseId, lastEditedTime);

        // 마지막 조회 시각 업데이트
        if (!changes.isEmpty()) {
            lastEditedTimes.put(databaseId, java.time.Instant.now().toString());
        }

        return changes;
    }
}

