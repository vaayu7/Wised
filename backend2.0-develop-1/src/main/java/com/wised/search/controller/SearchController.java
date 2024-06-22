package com.wised.search.controller;

import com.wised.search.dtos.SearchResponse;
import com.wised.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/v1/search")
@AllArgsConstructor
public class SearchController {


    private final SearchService searchService;

    @GetMapping("/user")
    public ResponseEntity<SearchResponse> searchUsers(@RequestParam String query) {
        try {
            searchService.saveSearchQuery(query);
            if (query.charAt(0) == '#') {
                return ResponseEntity.ok(searchService.searchPostAndProfileByHashTag(query));
            } else {
                return ResponseEntity.ok(searchService.searchUsers(query));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse
                            .builder()
                            .error("An error occurred while searching for users.")
                            .build());
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<SearchResponse> searchPosts(@RequestParam String query) {
        try {
            searchService.saveSearchQuery(query);
            if (query.charAt(0) == '#') {
                return ResponseEntity.ok(searchService.searchPostAndProfileByHashTag(query));
            } else {
                return ResponseEntity.ok(searchService.searchPosts(query));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse
                            .builder()
                            .error("An error occurred while searching for users.")
                            .build());
        }
    }

    @GetMapping("/")
    public ResponseEntity<SearchResponse> search(@RequestParam String query) {
        try {
            searchService.saveSearchQuery(query);
            if (query.charAt(0) == '#') {
                return ResponseEntity.ok(searchService.searchPostAndProfileByHashTag(query));
            } else {
                return ResponseEntity.ok(searchService.search(query));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse
                            .builder()
                            .error("An error occurred while searching for users.")
                            .build());
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<SearchResponse> getTrendingSearches() {
        try {
            SearchResponse trendingSearches = searchService.getTrendingSearches();
            return ResponseEntity.ok(trendingSearches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse
                            .builder()
                            .error("An error occurred while searching for users.")
                            .build());
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<SearchResponse> getRecentSearches() {
        try {
            SearchResponse recentSearches = searchService.getRecentSearches();
            return ResponseEntity.ok(recentSearches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SearchResponse
                            .builder()
                            .error("An error occurred while searching for users.")
                            .build());
        }
    }
}
