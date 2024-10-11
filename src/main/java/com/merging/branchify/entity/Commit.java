package com.merging.branchify.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="commits")
public class Commit {

    @Id
    private String sha; // GitHub에서 커밋을 식별하는 데 사용되는 고유 식별자가 SHA-1 해시 문자열이다.

    private String authorName;
    private String authorEMail;
    private String message;
    private String commitDate;
    private String repositoryName;
}
