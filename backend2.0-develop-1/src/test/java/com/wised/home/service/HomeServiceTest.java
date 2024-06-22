package com.wised.home.service;

import com.wised.home.exception.ContentRetrievalException;
import com.wised.home.dtos.ContentDto;
import com.wised.post.enums.LikeType;
import com.wised.post.model.Post;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import com.wised.post.repository.ShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HomeServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ShareRepository shareRepository;

    @InjectMocks
    private HomeService homeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSpotlight_Success() throws ContentRetrievalException {
        String topic = "TestTopic";
        Post post1 = new Post();
        Post post2 = new Post();
        List<Post> posts = Arrays.asList(post1, post2);

        when(postRepository.findPostByCategory(anyString())).thenReturn(posts);
        when(likeRepository.countByPostAndTypeAndDateRange(any(Post.class), eq(LikeType.LIKE), any(Date.class), any(Date.class)))
                .thenReturn(10);
        when(shareRepository.countUniqueUsersByPostAndDateRange(any(Post.class), any(Date.class), any(Date.class)))
                .thenReturn(5);

        ContentDto contentDto = homeService.getSpotlight(topic);

        assertEquals("Successfully retrieved the spotlight contents", contentDto.getMessage());
        assertEquals(2, contentDto.getPosts().size());
    }

    @Test
    void getSpotlight_Failure() {
        String topic = "TestTopic";

        when(postRepository.findPostByCategory(anyString())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            homeService.getSpotlight(topic);
        });

        assertEquals("Failed to retrieve top list: Database error", exception.getMessage());
    }

    @Test
    void getUpToDateContentsByCategory_Success() throws ContentRetrievalException {
        String topic = "TestTopic";
        Post post1 = new Post();
        Post post2 = new Post();
        List<Post> posts = Arrays.asList(post1, post2);

        when(postRepository.findPostByCategory(anyString())).thenReturn(posts);

        List<Post> result = homeService.getUpToDateContentsByCategory(topic);

        assertEquals(2, result.size());
        verify(postRepository, times(1)).findPostByCategory(topic);
    }

    @Test
    void getUpToDateContentsByCategory_Failure() {
        String topic = "TestTopic";

        when(postRepository.findPostByCategory(anyString())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            homeService.getUpToDateContentsByCategory(topic);
        });

        assertEquals("Failed to retrieve updated contents: Database error", exception.getMessage());
    }

    @Test
    void getPopularGenres_Success() throws ContentRetrievalException {
        String category = "TestCategory";
        Post post1 = new Post();
        post1.setCategory(com.wised.post.enums.Category.GENERAL);
        Post post2 = new Post();
        post2.setCategory(com.wised.post.enums.Category.MACHINE_LEARNING);
        List<Post> posts = Arrays.asList(post1, post2);

        when(postRepository.findPostByCategory(anyString())).thenReturn(posts);

        Map<String, List<Post>> result = homeService.getPopularGenres(category);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("GENERAL"));
        assertTrue(result.containsKey("TECHNOLOGY"));
        verify(postRepository, times(1)).findPostByCategory(category);
    }

    @Test
    void getPopularGenres_Failure() {
        String category = "TestCategory";

        when(postRepository.findPostByCategory(anyString())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            homeService.getPopularGenres(category);
        });

        assertEquals("Failed to retrieve popular genres: Database error", exception.getMessage());
    }
}
