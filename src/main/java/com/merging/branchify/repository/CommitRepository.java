package com.merging.branchify.repository;

import com.merging.branchify.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.merging.branchify.entity.CommitEntity;


public interface CommitRepository extends JpaRepository<CommitEntity, Long> {


}