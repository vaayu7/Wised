package com.wised.bystream.controller;

import com.wised.bystream.dtos.ContentDto;
import com.wised.bystream.dtos.StreamRequestDto;
import com.wised.bystream.service.ByStreamService;
import com.wised.post.model.Post;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/bystream")
public class ByStreamController {
    private final ByStreamService byStreamService;
    @GetMapping("/recommendation-new-user")
    public ResponseEntity<ContentDto> getRecommendationsForNewUser(@RequestParam StreamRequestDto streamRequestDto) {
        try{
            ContentDto recommendations = byStreamService.getRecommendationsForNewUser(streamRequestDto);
            return ResponseEntity.ok(recommendations);
        }catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/recommendation-existing-user")
    public ResponseEntity<ContentDto> getRecommendationsForExistingUser(
            @RequestParam StreamRequestDto streamRequestDto) {
        try{
            ContentDto recommendations = byStreamService.getRecommendationsForExistingUser(streamRequestDto);
            return ResponseEntity.ok(recommendations);
        }catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/notes")
    public ResponseEntity<List<Post>> getNotesByUniversity(
            @RequestParam StreamRequestDto streamRequestDto) {
        try{
            List<Post> notes = byStreamService.getNotesByUniversity(streamRequestDto);
            return ResponseEntity.ok(notes);
        }catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/question-papers")
    public ResponseEntity<Map<String, List<Post>>> getQuestionPapersByUniversity(
            @RequestParam StreamRequestDto streamRequestDto) {
        try{
            Map<String, List<Post>> questionPapers = byStreamService.getQuestionPapersByUniversity(streamRequestDto);
            return ResponseEntity.ok(questionPapers);
        }catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }


    }


    @GetMapping("/write-ups")
    public ResponseEntity<List<Post>> getWriteUpsByUniversity(
            @RequestParam StreamRequestDto streamRequestDto) {
        try{
            List<Post> writeUps = byStreamService.getWriteUpsByUniversity(streamRequestDto);
            return ResponseEntity.ok(writeUps);
        }catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/exam/write-ups")
    public ResponseEntity<Map<String, List<Post>>> getWriteUpsByExam(
            @RequestParam String stream) {
        try{
            Map<String, List<Post>> writeUps = byStreamService.getWriteUpsByExam(stream);
            return ResponseEntity.ok(writeUps);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/exam/question-papers")
    public ResponseEntity<Map<String, List<Post>>> getQuestionPapersByExam(
            @RequestParam String stream) {
        try{
            Map<String, List<Post>> questionPapers = byStreamService.getQuestionPapersByExam(stream);
            return ResponseEntity.ok(questionPapers);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/exam/up-to-date-contents")
    public ResponseEntity<List<Post>> getUpToDateContentsByExam(
            @RequestParam String stream) {
        try{
            List<Post> upToDateContents = byStreamService.getUpToDateContentsByExam(stream);
            return ResponseEntity.ok(upToDateContents);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }

    @GetMapping("/exam/notes")
    public ResponseEntity<List<Post>> getNotesByExam(
            @RequestParam String stream) {
        try{
            List<Post> notes = byStreamService.getNotesByExam(stream);
            return ResponseEntity.ok(notes);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

    }
}
