package com.merging.branchify.dto;

import lombok.Getter;

@Getter
public class CommitResponseDto extends ResponseDto{
    private String sha;
    private String message;
    private String author;
    private String date;

    public void setSha(String sha) {
        this.sha = sha;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setDate(String date) {
        this.date = date;
    }
//    public CommitDto.Commit getCommit() {
//        return commit;
//    }
}
