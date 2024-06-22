package com.wised.auth.service;

import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.School;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.CollegeRepository;
import com.wised.auth.repository.EducationRepository;
import com.wised.auth.repository.SchoolRepository;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.dtos.EducationDTO;
import com.wised.auth.dtos.EducationRequest;
import com.wised.auth.enums.EducationType;
import com.wised.auth.exception.EducationAdditionException;
import com.wised.auth.exception.EducationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EducationServiceTest {

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private EducationService educationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    private Date convertToDateViaInstant(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    @Test
    public void testGetEducation_Success() throws EducationNotFoundException {
        UserProfile userProfile = new UserProfile();
        List<Education> educationList = new ArrayList<>();
        Education education = new Education();
        education.setType(EducationType.COLLEGE);
        educationList.add(education);

        when(educationRepository.findByUser(userProfile)).thenReturn(Optional.of(educationList));
        when(collegeRepository.findById(anyInt())).thenReturn(Optional.of(new College()));
        when(schoolRepository.findById(anyInt())).thenReturn(Optional.of(new School()));

        List<EducationDTO> result = educationService.getEducation(userProfile);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(educationRepository, times(1)).findByUser(userProfile);
    }

    @Test
    public void testGetEducation_NotFound() {
        UserProfile userProfile = new UserProfile();

        when(educationRepository.findByUser(userProfile)).thenReturn(Optional.empty());

        assertThrows(EducationNotFoundException.class, () -> {
            educationService.getEducation(userProfile);
        });

        verify(educationRepository, times(1)).findByUser(userProfile);
    }

    @Test
    public void testGetCollege_Success() {
        Education education = new Education();
        education.setType(EducationType.COLLEGE);
        education.setTypeId(1);

        College college = new College();

        when(collegeRepository.findById(education.getTypeId())).thenReturn(Optional.of(college));

        College result = educationService.getCollege(education);

        assertNotNull(result);
        verify(collegeRepository, times(1)).findById(education.getTypeId());
    }

    @Test
    public void testGetCollege_NotFound() {
        Education education = new Education();
        education.setType(EducationType.COLLEGE);
        education.setTypeId(1);

        when(collegeRepository.findById(education.getTypeId())).thenReturn(Optional.empty());

        College result = educationService.getCollege(education);

        assertNull(result);
        verify(collegeRepository, times(1)).findById(education.getTypeId());
    }

    @Test
    public void testGetSchool_Success() {
        Education education = new Education();
        education.setType(EducationType.SCHOOL);
        education.setTypeId(1);

        School school = new School();

        when(schoolRepository.findById(education.getTypeId())).thenReturn(Optional.of(school));

        School result = educationService.getSchool(education);

        assertNotNull(result);
        verify(schoolRepository, times(1)).findById(education.getTypeId());
    }

    @Test
    public void testGetSchool_NotFound() {
        Education education = new Education();
        education.setType(EducationType.SCHOOL);
        education.setTypeId(1);

        when(schoolRepository.findById(education.getTypeId())).thenReturn(Optional.empty());

        School result = educationService.getSchool(education);

        assertNull(result);
        verify(schoolRepository, times(1)).findById(education.getTypeId());
    }

    @Test
    public void testAddEducation_Success() throws EducationAdditionException {
        UserProfile userProfile = new UserProfile();
        EducationRequest request = new EducationRequest();
        request.setType(EducationType.COLLEGE);
        request.setSpecializationStream("Science");
        request.setFromDate(convertToDateViaInstant(LocalDate.of(2022, 1, 1)));
        request.setToDate(convertToDateViaInstant(LocalDate.of(2023, 1, 1)));
        request.setCurrentlyStudying(true);
        request.setCurrentYearClass(1);
        request.setCurrentSemester(2);
        request.setUniversityName("University");
        request.setInstituteName("Institute");
        request.setLocation("Location");

        Education education = Education.builder()
                .type(request.getType())
                .specializationStream(request.getSpecializationStream())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .currentlyStudying(request.isCurrentlyStudying())
                .currentYearClass(request.getCurrentYearClass())
                .currentSemester(request.getCurrentSemester())
                .user(userProfile)
                .build();

        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(collegeRepository.save(any(College.class))).thenReturn(new College());

        Education result = educationService.addEducation(userProfile, request);

        assertNotNull(result);
        verify(educationRepository, times(2)).save(any(Education.class));
        verify(collegeRepository, times(1)).save(any(College.class));
    }

    @Test
    public void testAddEducation_Failure() {
        UserProfile userProfile = new UserProfile();
        EducationRequest request = new EducationRequest();

        when(educationRepository.save(any(Education.class))).thenThrow(RuntimeException.class);

        assertThrows(EducationAdditionException.class, () -> {
            educationService.addEducation(userProfile, request);
        });

        verify(educationRepository, times(1)).save(any(Education.class));
    }

    @Test
    public void testEditEducation_Success() throws EducationNotFoundException {
        UserProfile userProfile = new UserProfile();
        EducationRequest request = new EducationRequest();
        request.setType(EducationType.COLLEGE);
        request.setSpecializationStream("Science");
        request.setFromDate(convertToDateViaInstant(LocalDate.of(2022, 1, 1)));
        request.setToDate(convertToDateViaInstant(LocalDate.of(2023, 1, 1)));
        request.setCurrentlyStudying(true);
        request.setCurrentYearClass(1);
        request.setCurrentSemester(2);
        request.setUniversityName("University");
        request.setInstituteName("Institute");
        request.setLocation("Location");

        Education education = new Education();
        education.setId(1);
        education.setType(EducationType.COLLEGE);
        education.setTypeId(1);

        when(educationRepository.findById(anyInt())).thenReturn(Optional.of(education));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(collegeRepository.findById(anyInt())).thenReturn(Optional.of(new College()));

        Education result = educationService.editEducation(userProfile, request, 1);

        assertNotNull(result);
        verify(educationRepository, times(1)).findById(anyInt());
        verify(educationRepository, times(1)).save(any(Education.class));
    }

    @Test
    public void testEditEducation_NotFound() {
        UserProfile userProfile = new UserProfile();
        EducationRequest request = new EducationRequest();

        when(educationRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(EducationNotFoundException.class, () -> {
            educationService.editEducation(userProfile, request, 1);
        });

        verify(educationRepository, times(1)).findById(anyInt());
    }

    @Test
    public void testRemoveEducation_Success() {
        Education education = new Education();
        education.setId(1);

        when(educationRepository.findById(anyInt())).thenReturn(Optional.of(education));
        doNothing().when(educationRepository).delete(education);

        boolean result = educationService.removeEducation(1);

        assertTrue(result);
        verify(educationRepository, times(1)).findById(anyInt());
        verify(educationRepository, times(1)).delete(any(Education.class));
    }

    @Test
    public void testRemoveEducation_NotFound() {
        when(educationRepository.findById(anyInt())).thenReturn(Optional.empty());

        boolean result = educationService.removeEducation(1);

        assertFalse(result);
        verify(educationRepository, times(1)).findById(anyInt());
    }
}

