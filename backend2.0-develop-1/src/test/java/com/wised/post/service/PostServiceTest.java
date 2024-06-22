package com.wised.post.service;


import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.service.S3FileService;
import com.wised.helpandsettings.repository.DeactivateAndDeleteRepository;
import com.wised.helpandsettings.service.DeactivateAndDeleteService;
import com.wised.people.enums.ReportReason;
import com.wised.post.dtos.*;
import com.wised.post.enums.Category;
import com.wised.post.enums.LikeType;
import com.wised.post.enums.PostType;
import com.wised.post.model.*;
import com.wised.post.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private HashTagRepository hashtagRepository;
    @Mock
    private MentionRepository mentionRepository;
    @Mock
    private S3FileService s3FileService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private DeactivateAndDeleteService deactivateAndDeleteService;
    @Mock
    private DeactivateAndDeleteRepository deactivateAndDeleteRepository;
    @Mock
    private ReportPostRepository reportPostRepository;
    @Mock
    private ShareRepository shareRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPost_Success() throws IOException {
        UserProfile userProfile = new UserProfile();
        PostRequest postRequest = new PostRequest();
        postRequest.setDescription("Test Description");
        postRequest.setDocTitle("Test Doc Title");
        postRequest.setType(PostType.NONE);
        postRequest.setCategory(Category.GENERAL);
        postRequest.setOtherCategory("Other Category");
        postRequest.setUserMentionedIds(List.of(1, 2, 3));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("testfile.txt");
        postRequest.setFiles(List.of(file));

        when(s3FileService.generatePresignedUrl(anyString(), anyLong())).thenReturn("aws_url");
        doNothing().when(s3FileService).uploadFile(anyString(), any(MultipartFile.class));

        Post savedPost = Post.builder()
                .description("Test Description")
                .docTitle("Test Doc Title")
                .type(PostType.NONE)
                .category(Category.GENERAL)
                .otherCategory("Other Category")
                .awsUrl(List.of("aws_url"))
                .shareCount(0)
                .user(userProfile)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(postRepository.findById(anyInt())).thenReturn(Optional.of(savedPost));

        PostResponse response = postService.createpost(postRequest, userProfile);

        assertEquals("post created successfully", response.getMessage());
        assertEquals(1, response.getPosts().size());
        assertEquals("Test Description", response.getPosts().get(0).getDescription());
    }

    @Test
    void likeDislikePost_Success() {
        UserProfile userProfile = new UserProfile();
        Post post = new Post();
        post.setId(1);

        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));
        when(likeRepository.findByUserAndPost(any(UserProfile.class), any(Post.class))).thenReturn(null);

        postService.likeDislikePost(1, userProfile, LikeType.LIKE);

        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    void getUserPosts_Success() {
        UserProfile viewer = new UserProfile();
        UserProfile owner = new UserProfile();
        owner.setId(1);

        when(deactivateAndDeleteService.isUserProfileDeactivated(any(UserProfile.class))).thenReturn(false);
        when(postRepository.findByUser(any(UserProfile.class))).thenReturn(List.of(new Post()));

        PostResponse response = postService.getUserPosts(viewer, owner);

        assertEquals("User posts retrieved successfully", response.getMessage());
        assertEquals(1, response.getPosts().size());
    }

    @Test
    void getPostById_Success() {
        Post post = new Post();
        post.setId(1);

        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));

        PostByIdResponse response = postService.getPostById(1);

        assertEquals("Post Retrieve successfully", response.getMessage());
        assertNotNull(response.getPost());
    }

    @Test
    void getAllPosts_Success() {
        when(deactivateAndDeleteRepository.findByIsDeactivated(false)).thenReturn(List.of(new UserProfile()));
        when(postRepository.findByUserIn(anyList())).thenReturn(List.of(new Post()));

        PostResponse response = postService.getAllPosts();

        assertEquals("All posts retrieved successfully", response.getMessage());
        assertEquals(1, response.getPosts().size());
    }

    @Test
    void reportPosts_Success() {
        UserProfile reportingUser = new UserProfile();
        ReportPostRequest reportPostRequest = new ReportPostRequest();
        reportPostRequest.setReportedPostId(1);
        reportPostRequest.setReportReason(ReportReason.OTHER);
        reportPostRequest.setReportDescription("Report description");

        Post post = new Post();
        post.setId(1);

        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));
        when(reportPostRepository.save(any(ReportPost.class))).thenReturn(new ReportPost());

        PostResponse response = postService.reportPosts(reportingUser, reportPostRequest);

        assertEquals("Post Successfully Reported", response.getMessage());
    }

    @Test
    void sharePost_Success() {
        UserProfile userProfile = new UserProfile();
        Post post = new Post();
        post.setId(1);

        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));

        postService.SharePost(1, userProfile);

        verify(shareRepository, times(1)).save(any(Share.class));
    }
}
