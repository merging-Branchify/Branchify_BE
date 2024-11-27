package com.merging.branchify.betaTester;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetaTesterRepository extends JpaRepository<BetaTester, Long> {

    boolean existsByEmail(String email);

    @Query("SELECT t.email FROM BetaTester t")
    List<String> findAllEmails();
}
