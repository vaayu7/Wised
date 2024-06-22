package com.wised.auth.service;


import com.wised.auth.model.ProfessionalDetails;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.ProfessionalDetailsRepository;
import com.wised.auth.dtos.ProfessionalDetailRequest;
import com.wised.auth.dtos.ProfessionalDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfessionalDetailServiceTest {

    @InjectMocks
    private ProfessionalDetailService professionalDetailService;

    @Mock
    private ProfessionalDetailsRepository professionalDetailsRepository;

    private UserProfile userProfile;
    private ProfessionalDetailRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userProfile = new UserProfile();
        userProfile.setId(1);

        request = new ProfessionalDetailRequest();
        request.setCompanyName("Test Company");
        request.setIndustry("IT");
        request.setCurrentlyEmployed(true);
        request.setDesignation("Developer");
        request.setFromDate(convertToDateViaInstant(LocalDate.of(2022, 1, 1)));
        request.setToDate(convertToDateViaInstant(LocalDate.of(2023, 1, 1)));
        request.setLocation("New York");
    }
    private Date convertToDateViaInstant(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    @Test
    void testCreateProfessionalDetail() {
        ProfessionalDetails professionalDetails = ProfessionalDetails.builder()
                .userProfile(userProfile)
                .companyName(request.getCompanyName())
                .industry(request.getIndustry())
                .isCurrentlyEmployed(request.isCurrentlyEmployed())
                .designation(request.getDesignation())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .location(request.getLocation())
                .build();

        when(professionalDetailsRepository.save(any(ProfessionalDetails.class))).thenReturn(professionalDetails);

        ProfessionalDetailResponse response = professionalDetailService.createProfessionalDetail(userProfile, request);

        assertTrue(response.isSuccess());
        assertEquals("Professional detail created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(request.getCompanyName(), response.getData().get(0).getCompanyName());
    }

    @Test
    void testGetProfessionalDetailByIdSuccess() {
        ProfessionalDetails professionalDetails = ProfessionalDetails.builder()
                .id(1)
                .userProfile(userProfile)
                .companyName("Test Company")
                .build();

        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.of(professionalDetails));

        ProfessionalDetailResponse response = professionalDetailService.getProfessionalDetailById(userProfile, 1);

        assertTrue(response.isSuccess());
        assertEquals("Professional detail retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("Test Company", response.getData().get(0).getCompanyName());
    }

    @Test
    void testGetProfessionalDetailByIdNotFound() {
        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.empty());

        ProfessionalDetailResponse response = professionalDetailService.getProfessionalDetailById(userProfile, 1);

        assertFalse(response.isSuccess());
        assertEquals("Professional detail not found for the given ID", response.getError());
    }

    @Test
    void testUpdateProfessionalDetailSuccess() {
        ProfessionalDetails professionalDetails = ProfessionalDetails.builder()
                .id(1)
                .userProfile(userProfile)
                .companyName("Old Company")
                .build();

        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.of(professionalDetails));
        when(professionalDetailsRepository.save(any(ProfessionalDetails.class))).thenReturn(professionalDetails);

        ProfessionalDetailResponse response = professionalDetailService.updateProfessionalDetail(userProfile, 1, request);

        assertTrue(response.isSuccess());
        assertEquals("Professional detail updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(request.getCompanyName(), response.getData().get(0).getCompanyName());
    }

    @Test
    void testUpdateProfessionalDetailNotFound() {
        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.empty());

        ProfessionalDetailResponse response = professionalDetailService.updateProfessionalDetail(userProfile, 1, request);

        assertFalse(response.isSuccess());
        assertEquals("Professional detail not found for the given ID", response.getError());
    }

    @Test
    void testDeleteProfessionalDetailSuccess() {
        ProfessionalDetails professionalDetails = ProfessionalDetails.builder()
                .id(1)
                .userProfile(userProfile)
                .build();

        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.of(professionalDetails));

        ProfessionalDetailResponse response = professionalDetailService.deleteProfessionalDetail(userProfile, 1);

        assertTrue(response.isSuccess());
        assertEquals("Professional detail deleted successfully", response.getMessage());
        verify(professionalDetailsRepository, times(1)).delete(professionalDetails);
    }

    @Test
    void testDeleteProfessionalDetailNotFound() {
        when(professionalDetailsRepository.findById(1)).thenReturn(Optional.empty());

        ProfessionalDetailResponse response = professionalDetailService.deleteProfessionalDetail(userProfile, 1);

        assertFalse(response.isSuccess());
        assertEquals("Professional detail not found for the given ID", response.getError());
    }
}

