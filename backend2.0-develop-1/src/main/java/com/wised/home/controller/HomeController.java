package com.wised.home.controller;

import com.wised.home.dtos.ContentDto;
import com.wised.home.exception.ContentRetrievalException;
import com.wised.home.service.HomeService;
import com.wised.post.model.Post;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/spotlight")
    public ResponseEntity<ContentDto> getSpotlight(@RequestParam String topic) {
        try {
            ContentDto recommendations = homeService.getSpotlight(topic);
            return ResponseEntity.ok(recommendations);
        } catch (ContentRetrievalException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ContentDto.builder().error("Failed to retrieve spotlight contents").build()
            );
        }
    }

    @GetMapping("/updated-contents")
    public ResponseEntity<ContentDto> getUpdatedContents(@RequestParam String topic) {
        try {
            List<Post> updatedContents = homeService.getUpToDateContentsByCategory(topic);
            return ResponseEntity.ok(ContentDto.builder()
                    .message("Successfully retrieved updated contents")
                    .posts(updatedContents)
                    .build());
        } catch (ContentRetrievalException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ContentDto.builder().error("Failed to retrieve updated contents").build()
            );
        }
    }

    @GetMapping("/popular-genres")
    public ResponseEntity<ContentDto> getPopularGenres(@RequestParam String topic) {
        try {
            Map<String, List<Post>> popularGenres = homeService.getPopularGenres(topic);
            return ResponseEntity.ok(ContentDto.builder()
                    .message("Successfully retrieved popular genres")
                    .mappedPosts(popularGenres)
                    .build());
        } catch (ContentRetrievalException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ContentDto.builder().error("Failed to retrieve popular genres").build()
            );
        }
    }
}
