package com.merging.branchify.user;

import lombok.Getter;

@Getter
public class User
{
    private String userId;
    private String userName;
    private String userEmail;
    private String gitToken;

    // 생성자
    public User(String userId, String userName, String email, String gitToken) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = email;
        this.gitToken = gitToken;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }
}
