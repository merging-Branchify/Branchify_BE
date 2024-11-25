package com.merging.branchify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merging.branchify.dto.NotionDatabaseDTO;
import com.merging.branchify.entity.NotionDatabase;
import com.merging.branchify.respository.NotionDataRepository;
import com.merging.branchify.respository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotionService {

    @Value("${notion.integration-token}")
    private String integrationToken;

    @Value("${notion.api-version}")
    private String notionVersion;

    private final UserTokenRepository userTokenRepository;
    private final NotionDataRepository notionDataRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotionService(UserTokenRepository userTokenRepository, NotionDataRepository notionDataRepository) {
        this.userTokenRepository = userTokenRepository;
        this.notionDataRepository = notionDataRepository;
    }

    // 데이터베이스 선택 저장
    public Map<String, Object> saveDatabaseSelection(String userId, String databaseId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // NotionDatabase 엔티티 생성 및 저장
            NotionDatabase notionDatabase = new NotionDatabase();
            notionDatabase.setUserId(userId);
            notionDatabase.setSelectedDatabaseId(databaseId);
            Instant now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
            notionDatabase.setLastChecked(now);;
            notionDataRepository.save(notionDatabase);

            // 응답 데이터 구성
            response.put("status", "success");
            response.put("message", "Database selection saved successfully.");
            System.out.println("User " + userId + " selected database: " + databaseId);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "failure");
            response.put("error", e.getMessage());
        }

        return response;
    }

    // 데이터베이스 목록 조회
    public List<NotionDatabaseDTO> listDatabases() {
        HttpHeaders headers = createHeaders();
        String requestBody = "{ \"filter\": { \"property\": \"object\", \"value\": \"database\" } }";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.notion.com/v1/search", HttpMethod.POST, entity, String.class
        );

        List<NotionDatabaseDTO> databases = new ArrayList<>();
        try{
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    String id = node.get("id").asText();
                    String title = node.get("title").get(0).get("text").get("content").asText();
                    databases.add(new NotionDatabaseDTO(id, title));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databases;
    }

    // 선택된 데이터베이스의 전체 내용 조회
    public List<Map<String, Object>> getAllContents(String databaseId) {
        HttpHeaders headers = createHeaders();

        // 빈 Body를 사용하여 필터 없이 모든 데이터 요청
        String requestBody = "{}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format("https://api.notion.com/v1/databases/%s/query", databaseId);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return extractAllProperties(response.getBody());
    }

    // 선택된 데이터베이스의 변경된 데이터 조회
    public List<Map<String, Object>> getModifiedContents(String databaseId, String lastEditedTime) {
        HttpHeaders headers = createHeaders();

        String requestBody = String.format("""
                {
                    "filter": {
                        "timestamp": "last_edited_time",
                        "last_edited_time": {
                            "after": "%s"
                        }
                    }
                }
                """, lastEditedTime, lastEditedTime);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format("https://api.notion.com/v1/databases/%s/query", databaseId);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return extractAllProperties(response.getBody());
    }

    private List<Map<String, Object>> extractAllProperties(String jsonResponse) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode page : results) {
                    Map<String, Object> pageData = new HashMap<>();
                    pageData.put("id", page.get("id").asText());
                    pageData.put("created_time", page.get("created_time").asText());
                    pageData.put("last_edited_time", page.get("last_edited_time").asText());

                    // 모든 속성을 동적으로 처리
                    Map<String, Object> propertiesData = new HashMap<>();
                    JsonNode properties = page.get("properties");

                    if (properties != null) {
                        properties.fieldNames().forEachRemaining(propertyName -> {
                            JsonNode property = properties.get(propertyName);
                            if (property == null || property.isNull()) {
                                return; // null 속성은 추가하지 않음
                            }

                            String type = property.get("type").asText();
                            Object value = null;

                            // 타입별 데이터 처리
                            switch (type) {
                                case "title":
                                    value = extractTitle(property);
                                    break;
                                case "rich_text":
                                    value = extractRichText(property);
                                    break;
                                case "number":
                                    value = property.has("number") && !property.get("number").isNull()
                                            ? property.get("number").asDouble() : null;
                                    break;
                                case "select":
                                    value = property.has("select") && !property.get("select").isNull()
                                            ? property.get("select").get("name").asText() : null;
                                    break;
                                case "status":
                                    value = extractStatus(property);
                                    break;
                                case "multi_select":
                                    value = extractMultiSelect(property);
                                    break;
                                case "date":
                                    value = extractDate(property);
                                    break;
                                case "people":
                                    value = extractPeople(property);
                                    break;
                                case "checkbox":
                                    value = property.has("checkbox") ? property.get("checkbox").asBoolean() : null;
                                    break;
                                case "url":
                                    value = property.has("url") && !property.get("url").isNull()
                                            ? property.get("url").asText() : null;
                                    break;
                                case "email":
                                    value = property.has("email") && !property.get("email").isNull()
                                            ? property.get("email").asText() : null;
                                    break;
                                case "phone_number":
                                    value = property.has("phone_number") && !property.get("phone_number").isNull()
                                            ? property.get("phone_number").asText() : null;
                                    break;
                                case "relation":
                                    value = extractRelation(property);
                                    break;
                                case "formula":
                                    value = extractFormula(property);
                                    break;
                                default:
                                    value = "Unsupported type: " + type;
                            }

                            // 값이 null이 아니면 속성 추가
                            if (value != null) {
                                propertiesData.put(propertyName, value);
                            }
                        });
                    }

                    pageData.put("properties", propertiesData);
                    pageData.put("url", page.get("url").asText());
                    pages.add(pageData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pages;
    }

    private List<String> extractTitle(JsonNode property) {
        List<String> titles = new ArrayList<>();
        property.get("title").forEach(titleNode -> {
            String content = titleNode.get("text").get("content").asText();
            titles.add(content);
        });
        return titles;
    }

    private String extractStatus(JsonNode property) {
        return property.get("status").isNull() ? null : property.get("status").get("name").asText();
    }

    private List<String> extractRichText(JsonNode property) {
        List<String> texts = new ArrayList<>();
        property.get("rich_text").forEach(textNode -> {
            String content = textNode.get("text").get("content").asText();
            texts.add(content);
        });
        return texts;
    }

    private List<String> extractMultiSelect(JsonNode property) {
        List<String> multiSelectValues = new ArrayList<>();
        property.get("multi_select").forEach(selectNode -> {
            multiSelectValues.add(selectNode.get("name").asText());
        });
        return multiSelectValues;
    }

    private Map<String, String> extractDate(JsonNode property) {
        Map<String, String> dateRange = new HashMap<>();
        JsonNode dateNode = property.get("date");
        if (dateNode != null) {
            dateRange.put("start", dateNode.get("start").isNull() ? null : dateNode.get("start").asText());
            dateRange.put("end", dateNode.get("end").isNull() ? null : dateNode.get("end").asText());
        }
        return dateRange;
    }

    private List<String> extractPeople(JsonNode property) {
        List<String> people = new ArrayList<>();
        property.get("people").forEach(person -> {
            String name = person.get("name").isNull() ? "Unknown" : person.get("name").asText();
            people.add(name);
        });
        return people;
    }

    private Object extractRelation(JsonNode property) {
        List<String> relatedIds = new ArrayList<>();
        property.get("relation").forEach(relation -> {
            relatedIds.add(relation.get("id").asText());
        });
        return relatedIds;
    }

    private Object extractFormula(JsonNode property) {
        return property.get("formula").get("string").asText();
    }

    // Notion API 요청 헤더 생성
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(integrationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Notion-Version", notionVersion);

        return headers;
    }
}