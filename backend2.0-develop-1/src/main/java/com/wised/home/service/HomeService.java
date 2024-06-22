package com.wised.home.service;

import com.wised.home.exception.ContentRetrievalException;
import com.wised.home.dtos.ContentDto;
import com.wised.post.enums.LikeType;
import com.wised.post.model.Post;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import com.wised.post.repository.ShareRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HomeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ShareRepository shareRepository;

    public ContentDto getSpotlight(String topic) throws ContentRetrievalException {
        try {
            // Fetch posts by category
            List<Post> allPosts = postRepository.findPostByCategory(topic);

            // Calculate weights for each post based on likes and shares
            Map<Post, Double> postWeight = calculatePostWeights(allPosts, topic);

            // Get the top 10 posts
            List<Post> top10Posts = getTopPosts(postWeight, 10);

            // Return the results wrapped in ContentDto
            return ContentDto.builder()
                    .message("Successfully retrieved the spotlight contents")
                    .posts(top10Posts)
                    .build();
        } catch (Exception e) {
            // Throw a custom exception in case of errors
            throw new ContentRetrievalException("Failed to retrieve top list: " + e.getMessage());
        }
    }

    private Map<Post, Double> calculatePostWeights(List<Post> posts, String stream) {
        int totalShare = 0;
        int totalLikeCount = 0;

        // Define date range (past 30 days)
        LocalDateTime currentDate = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime tomorrowDateTime = currentDate.plusDays(1);
        LocalDateTime startDate = currentDate.minusDays(30);

        Date currentDateAsDate = Date.valueOf(tomorrowDateTime.toLocalDate());
        Date startDateAsDate = Date.valueOf(startDate.toLocalDate());

        // Map to store posts with their share and like counts
        Map<Post, TopShares> postMap = new HashMap<>();
        for (Post post : posts) {
            int likesCount = likeRepository.countByPostAndTypeAndDateRange(post, LikeType.LIKE, startDateAsDate, currentDateAsDate);
            int shareCount = shareRepository.countUniqueUsersByPostAndDateRange(post, startDateAsDate, currentDateAsDate);
            totalShare += shareCount;
            totalLikeCount += likesCount;
            postMap.put(post, new TopShares(shareCount, likesCount));
        }

        // Calculate weights for each post
        Map<Post, Double> postWeight = new HashMap<>();
        for (Map.Entry<Post, TopShares> entry : postMap.entrySet()) {
            Post currentPost = entry.getKey();
            TopShares topShare = entry.getValue();
            int postLikes = topShare.postLikes;
            int postShares = topShare.postShares;
            double w1 = totalShare == 0 ? 0 : (double) postShares / totalShare;
            double w2 = totalLikeCount == 0 ? 0 : (double) postLikes / totalLikeCount;
            double weight = w1 * 0.4 + w2 * 0.6;
            postWeight.put(currentPost, weight);
        }

        return postWeight;
    }

    private List<Post> getTopPosts(Map<Post, Double> postWeight, int limit) {
        // Sort posts by weight and return the top ones
        return postWeight.entrySet().stream()
                .sorted(Map.Entry.<Post, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Post> getUpToDateContentsByCategory(String topic) throws ContentRetrievalException {
        try {
            // Fetch posts by category
            return postRepository.findPostByCategory(topic);
        } catch (Exception e) {
            throw new ContentRetrievalException("Failed to retrieve updated contents: " + e.getMessage());
        }
    }

    public Map<String, List<Post>> getPopularGenres(String category) throws ContentRetrievalException {
        try {
            // Fetch all posts by category
            List<Post> allPosts = postRepository.findPostByCategory(category);

            // Group posts by their category name
            return allPosts.stream().collect(Collectors.groupingBy(post -> post.getCategory().name()));
        } catch (Exception e) {
            throw new ContentRetrievalException("Failed to retrieve popular genres: " + e.getMessage());
        }
    }
}