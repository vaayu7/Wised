package com.wised.search.repository;

import com.wised.search.model.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, Integer> {

    List<SearchQuery> findTop10ByOrderByCreatedAtDesc();

    List<SearchQuery> findByCreatedAtAfter(Date oneDayAgo);
}
