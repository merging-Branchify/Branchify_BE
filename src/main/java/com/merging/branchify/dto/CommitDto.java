
package com.merging.branchify.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommitDto {
    private String sha;
    private Commit commit;

    @Getter
    @Setter
    public static class Commit {
        private String author;
        private String message;

        @Getter
        @Setter
        public static class Author {
            private String name;
            private String date;
        }

    }
}
