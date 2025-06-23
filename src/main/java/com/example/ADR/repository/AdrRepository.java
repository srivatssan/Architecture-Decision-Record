package com.example.ADR.repository;

import com.example.ADR.model.ADR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdrRepository extends JpaRepository<ADR, Long> {
    @Query("SELECT a FROM ADR a WHERE a.deleted = false ORDER BY a.createdAt DESC")
    List<ADR> findTop10ByOrderByCreatedAtDesc(); // Fetch top 10 recent ADRs
    @Query("SELECT a FROM ADR a WHERE a.deleted = false ORDER BY a.createdAt DESC")
    List<ADR> findAllNotDeleted();
}

