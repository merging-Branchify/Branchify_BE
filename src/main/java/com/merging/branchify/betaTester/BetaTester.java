package com.merging.branchify.betaTester;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BetaTester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
}
