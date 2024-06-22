package com.wised.bystream.service;

import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.CollegeRepository;
import com.wised.auth.repository.EducationRepository;
import com.wised.bystream.dtos.ContentDto;
import com.wised.bystream.dtos.StreamRequestDto;
import com.wised.people.dtos.FolloweeAndFollowingResponse;
import com.wised.people.service.PeopleService;
import com.wised.post.enums.PostType;
import com.wised.post.model.Post;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import com.wised.post.repository.ShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ByStreamServiceTest {

    @InjectMocks
    private ByStreamService byStreamService;

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ShareRepository shareRepository;

    @Mock
    private PeopleService peopleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRecommendationsForNewUser() {
        StreamRequestDto streamRequestDto = new StreamRequestDto();
        streamRequestDto.setUniversity("TestUniversity");
        streamRequestDto.setStream("TestStream");
        streamRequestDto.setSem(1);

        List<Post> posts = new ArrayList<>();
        when(collegeRepository.findByUniversityName(anyString())).thenReturn(new ArrayList<>());
        when(postRepository.findByUser(any(UserProfile.class))).thenReturn(posts);

        ContentDto contentDto = byStreamService.getRecommendationsForNewUser(streamRequestDto);

        assertNotNull(contentDto);
        assertEquals("Successfully retrieved top recommendation", contentDto.getMessege());
    }

    @Test
    void testGetRecommendationsForExistingUser() {
        StreamRequestDto streamRequestDto = new StreamRequestDto();
        streamRequestDto.setUniversity("TestUniversity");
        streamRequestDto.setStream("TestStream");
        streamRequestDto.setSem(1);

        // Create a mock FolloweeAndFollowingResponse object
        FolloweeAndFollowingResponse followeeAndFollowingResponse = mock(FolloweeAndFollowingResponse.class);

        // Create a mock list of UserProfile
        List<UserProfile> followingList = new ArrayList<>();
        when(followeeAndFollowingResponse.getData()).thenReturn(followingList);

        // Mock the getFollowingList method to return the mock FolloweeAndFollowingResponse
        when(peopleService.getFollowingList()).thenReturn(followeeAndFollowingResponse);

        List<Post> posts = new ArrayList<>();
        when(collegeRepository.findByUniversityName(anyString())).thenReturn(new ArrayList<>());
        when(postRepository.findByUser(any(UserProfile.class))).thenReturn(posts);

        ContentDto contentDto = byStreamService.getRecommendationsForExistingUser(streamRequestDto);

        assertNotNull(contentDto);
        assertEquals("Successfully retrieved top recommendation", contentDto.getMessege());
    }

    @Test
    void testGetNotesByUniversity() {
        StreamRequestDto streamRequestDto = new StreamRequestDto();
        streamRequestDto.setUniversity("TestUniversity");
        streamRequestDto.setStream("TestStream");
        streamRequestDto.setSem(1);

        List<College> colleges = new ArrayList<>();
        when(collegeRepository.findByUniversityName(anyString())).thenReturn(colleges);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.NOTES))).thenReturn(posts);

        List<Post> result = byStreamService.getNotesByUniversity(streamRequestDto);

        assertNotNull(result);
    }

    @Test
    void testGetQuestionPapersByUniversity() {
        StreamRequestDto streamRequestDto = new StreamRequestDto();
        streamRequestDto.setUniversity("TestUniversity");
        streamRequestDto.setStream("TestStream");
        streamRequestDto.setSem(1);

        List<College> colleges = new ArrayList<>();
        when(collegeRepository.findByUniversityName(anyString())).thenReturn(colleges);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.QUESTION_PAPERS))).thenReturn(posts);

        Map<String, List<Post>> result = byStreamService.getQuestionPapersByUniversity(streamRequestDto);

        assertNotNull(result);
    }

    @Test
    void testGetWriteUpsByUniversity() {
        StreamRequestDto streamRequestDto = new StreamRequestDto();
        streamRequestDto.setUniversity("TestUniversity");
        streamRequestDto.setStream("TestStream");
        streamRequestDto.setSem(1);

        List<College> colleges = new ArrayList<>();
        when(collegeRepository.findByUniversityName(anyString())).thenReturn(colleges);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.NONE))).thenReturn(posts);

        List<Post> result = byStreamService.getWriteUpsByUniversity(streamRequestDto);

        assertNotNull(result);
    }

    @Test
    void testGetWriteUpsByExam() {
        String stream = "TestStream";

        List<Education> educations = new ArrayList<>();
        when(educationRepository.findBySpecializationStream(anyString())).thenReturn(educations);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.NONE))).thenReturn(posts);

        Map<String, List<Post>> result = byStreamService.getWriteUpsByExam(stream);

        assertNotNull(result);
    }

    @Test
    void testGetQuestionPapersByExam() {
        String stream = "TestStream";

        List<Education> educations = new ArrayList<>();
        when(educationRepository.findBySpecializationStream(anyString())).thenReturn(educations);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.QUESTION_PAPERS))).thenReturn(posts);

        Map<String, List<Post>> result = byStreamService.getQuestionPapersByExam(stream);

        assertNotNull(result);
    }

    @Test
    void testGetUpToDateContentsByExam() {
        String stream = "TestStream";

        List<Education> educations = new ArrayList<>();
        when(educationRepository.findBySpecializationStream(anyString())).thenReturn(educations);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.UP_TO_DATE_CONTENTS))).thenReturn(posts);

        List<Post> result = byStreamService.getUpToDateContentsByExam(stream);

        assertNotNull(result);
    }

    @Test
    void testGetNotesByExam() {
        String stream = "TestStream";

        List<Education> educations = new ArrayList<>();
        when(educationRepository.findBySpecializationStream(anyString())).thenReturn(educations);

        List<Post> posts = new ArrayList<>();
        when(postRepository.findByUserAndType(any(UserProfile.class), eq(PostType.NOTES))).thenReturn(posts);

        List<Post> result = byStreamService.getNotesByExam(stream);

        assertNotNull(result);
    }
}
