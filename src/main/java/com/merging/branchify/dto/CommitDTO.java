package com.merging.branchify.dto;

import lombok.Data;

@Data
public class CommitDTO {

    private String sha; // 커밋의 고유 SHA-1 해시 값
    private CommitDetails commit; // 커밋 세부 정보

    @Data
    public static class CommitDetails {
        private CommitAuthor author; // 커밋 작성자 정보
        private String message; // 커밋 메세지

        @Data
        public static class CommitAuthor {
            private String name; // 작성자 이름
            private String email; // 작성자 이메일
            private String date; // 커밋 날짜
        }
    }
}
