package domain;

import lombok.Getter;

@Getter
public class Member {
    private Long id;
    private String name;
    private String gitURL;

    public void setId(Long id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setGitURL(String gitURL){
        this.gitURL = gitURL;
    }
}
