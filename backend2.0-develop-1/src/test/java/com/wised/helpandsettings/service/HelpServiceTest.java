package com.wised.helpandsettings.service;

import com.wised.auth.model.UserProfile;
import com.wised.auth.service.S3FileService;
import com.wised.helpandsettings.dtos.HelpRequest;
import com.wised.helpandsettings.dtos.HelpResponse;
import com.wised.helpandsettings.dtos.HelpResponseDTO;
import com.wised.helpandsettings.enums.HelpStatus;
import com.wised.helpandsettings.exception.HelpNotFoundException;
import com.wised.helpandsettings.model.Help;
import com.wised.helpandsettings.repository.HelpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HelpServiceTest {

    @Mock
    private HelpRepository helpRepository;

    @Mock
    private S3FileService s3FileService;

    @InjectMocks
    private HelpService helpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createHelpRequest_Success() throws IOException {
        UserProfile userProfile = new UserProfile();
        HelpRequest helpRequest = new HelpRequest();
        helpRequest.setName("Test User");
        helpRequest.setEmail("test@example.com");
        helpRequest.setSubject("Test Subject");
        helpRequest.setIssue(new String[]{"Test Issue"});
        helpRequest.setDescription("Test Description");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("testfile.txt");
        helpRequest.setFiles(new MultipartFile[]{file});  // Set as array

        when(s3FileService.generatePresignedUrl(anyString(), anyLong())).thenReturn("aws_url");
        doNothing().when(s3FileService).uploadFile(anyString(), any(MultipartFile.class));

        Help savedHelp = Help.builder()
                .name("Test User")
                .email("test@example.com")
                .subject("Test Subject")
                .issue(new String[]{"Test Issue"})
                .description("Test Description")
                .awsUrl(List.of("aws_url"))
                .helpStatus(HelpStatus.UNDER_REVIEW)
                .userProfile(userProfile)
                .build();

        when(helpRepository.save(any(Help.class))).thenReturn(savedHelp);

        HelpResponse response = helpService.createHelpRequest(userProfile, helpRequest);

        assertTrue(response.isSuccess());
        assertEquals("Help requests retrieved successfully", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals("Test User", response.getData().get(0).getName());
    }

    @Test
    void updateHelpStatus_Success() {
        Help help = Help.builder().helpStatus(HelpStatus.UNDER_REVIEW).build();
        when(helpRepository.findById(anyInt())).thenReturn(Optional.of(help));

        boolean result = helpService.updateHelpStatus(1, HelpStatus.RESOLVED);

        assertTrue(result);
        verify(helpRepository, times(1)).save(help);
    }

    @Test
    void updateHelpStatus_HelpNotFound() {
        when(helpRepository.findById(anyInt())).thenReturn(Optional.empty());

        boolean result = helpService.updateHelpStatus(1, HelpStatus.RESOLVED);

        assertFalse(result);
    }

    @Test
    void getAllHelpRequests_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findAll()).thenReturn(helpList);

        List<Help> result = helpService.getAllHelpRequests();

        assertEquals(1, result.size());
    }

    @Test
    void getHelpRequestById_Success() {
        Help help = new Help();
        when(helpRepository.findById(anyInt())).thenReturn(Optional.of(help));

        Optional<Help> result = helpService.getHelpRequestById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void getHelpRequestsByStatus_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findByHelpStatus(any(HelpStatus.class))).thenReturn(helpList);

        List<Help> result = helpService.getHelpRequestsByStatus(HelpStatus.UNDER_REVIEW);

        assertEquals(1, result.size());
    }

    @Test
    void getHelpRequestsByUser_Success() {
        UserProfile userProfile = new UserProfile();
        List<Help> helpList = new ArrayList<>();
        Help help = Help.builder()
                .name("Test User")
                .email("test@example.com")
                .subject("Test Subject")
                .issue(new String[]{"Test Issue"})
                .description("Test Description")
                .awsUrl(List.of("aws_url"))
                .helpStatus(HelpStatus.UNDER_REVIEW)
                .userProfile(userProfile)
                .build();
        helpList.add(help);

        when(helpRepository.findByUserProfile(any(UserProfile.class))).thenReturn(helpList);

        HelpResponse response = helpService.getHelpRequestsByUser(userProfile);

        assertTrue(response.isSuccess());
        assertEquals("Help requests retrieved successfully", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals("Test User", response.getData().get(0).getName());
    }

    @Test
    void getHelpRequestsByUserEmail_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findByEmail(anyString())).thenReturn(helpList);

        List<Help> result = helpService.getHelpRequestsByUserEmail("test@example.com");

        assertEquals(1, result.size());
    }

    @Test
    void getHelpRequestsBySubject_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findBySubject(anyString())).thenReturn(helpList);

        List<Help> result = helpService.getHelpRequestsBySubject("Test Subject");

        assertEquals(1, result.size());
    }

    @Test
    void getHelpRequestsByIssue_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findByIssue(anyString())).thenReturn(helpList);

        List<Help> result = helpService.getHelpRequestsByIssue("Test Issue");

        assertEquals(1, result.size());
    }

    @Test
    void getHelpRequestsByDescription_Success() {
        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help());
        when(helpRepository.findByDescription(anyString())).thenReturn(helpList);

        List<Help> result = helpService.getHelpRequestsByDescription("Test Description");

        assertEquals(1, result.size());
    }
}
