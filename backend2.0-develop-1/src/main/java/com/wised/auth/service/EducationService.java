package com.wised.auth.service;

import com.wised.auth.dtos.EducationDTO;
import com.wised.auth.dtos.EducationRequest;
import com.wised.auth.enums.EducationType;
import com.wised.auth.exception.EducationAdditionException;
import com.wised.auth.exception.EducationNotFoundException;
import com.wised.auth.mappers.EducationMapper;
import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.School;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.CollegeRepository;
import com.wised.auth.repository.EducationRepository;
import com.wised.auth.repository.SchoolRepository;
import com.wised.auth.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EducationService {
    private final EducationRepository educationRepository;
    private final CollegeRepository collegeRepository;
    private final SchoolRepository schoolRepository;

    private final UserProfileRepository userProfileRepository;

    public List<EducationDTO> getEducation(UserProfile userProfile) throws EducationNotFoundException {
        Optional<List<Education>> optionalEducationList = educationRepository.findByUser(userProfile);
        if(optionalEducationList.isPresent()){
            List<Education> educationList = optionalEducationList.get();
            List<EducationDTO> mappedEducationList = new ArrayList<>();
            for(Education education : educationList){

                College collegeData = getCollege(education);
                School schoolData = getSchool(education);

                EducationDTO educationDTO = EducationMapper.mapEducationToEducationDTO(education, collegeData, schoolData);

                mappedEducationList.add(educationDTO);
            }
            return mappedEducationList;
        }
        else{
            throw new EducationNotFoundException("No Education Found");
        }
    }

    public College getCollege(Education education) {
        if (education.getType() == EducationType.COLLEGE && education.getTypeId() != null) {
            return collegeRepository.findById(education.getTypeId()).orElse(null);
        }
        return null;
    }

    public School getSchool(Education education) {
        if (education.getType() == EducationType.SCHOOL && education.getTypeId() != null) {
            return schoolRepository.findById(education.getTypeId()).orElse(null);
        }
        return null;
    }

    public Education addEducation(UserProfile userProfile, EducationRequest requestDTO) throws EducationAdditionException {
        try {
            Education education = Education.builder()
                    .type(requestDTO.getType())
                    .specializationStream(requestDTO.getSpecializationStream())
                    .fromDate(requestDTO.getFromDate())
                    .toDate(requestDTO.getToDate())
                    .currentlyStudying(requestDTO.isCurrentlyStudying())
                    .currentYearClass(requestDTO.getCurrentYearClass())
                    .currentSemester(requestDTO.getCurrentSemester())
                    .user(userProfile)
                    .build();

            educationRepository.save(education);

            if (requestDTO.getType() == EducationType.COLLEGE) {
                College college = College.builder()
                        .universityName(requestDTO.getUniversityName())
                        .instituteName(requestDTO.getInstituteName())
                        .location(requestDTO.getLocation())
                        .education(education)
                        .build();
                collegeRepository.save(college);
                education.setTypeId(college.getId());
            } else if (requestDTO.getType() == EducationType.SCHOOL) {
                School school = School.builder()
                        .boardName(requestDTO.getBoardName())
                        .schoolName(requestDTO.getSchoolName())
                        .location(requestDTO.getLocation())
                        .education(education)
                        .build();
                schoolRepository.save(school);
                education.setTypeId(school.getId());
            }

            return educationRepository.save(education);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EducationAdditionException("Failed to add education. Please try again later.", e);
        }
    }
    public Education editEducation(UserProfile userProfile, EducationRequest requestDTO, int id) throws EducationNotFoundException {
        try {
            Optional<Education> optionalEducation = educationRepository.findById(id);
            if(!optionalEducation.isPresent()){
                throw new EducationNotFoundException("No education found with the given id");
            }
            Education education = optionalEducation.get();
            Education modifiedEducation = Education.builder()
                    .type(requestDTO.getType())
                    .specializationStream(requestDTO.getSpecializationStream())
                    .fromDate(requestDTO.getFromDate())
                    .toDate(requestDTO.getToDate())
                    .currentlyStudying(requestDTO.isCurrentlyStudying())
                    .currentYearClass(requestDTO.getCurrentYearClass())
                    .currentSemester(requestDTO.getCurrentSemester())
                    .user(userProfile)
                    .build();
            educationRepository.save(modifiedEducation);

            if (requestDTO.getType() == EducationType.COLLEGE) {
                int collegeId = modifiedEducation.getTypeId();
                Optional<College> optionalCollege = collegeRepository.findById(collegeId);
                if(optionalCollege.isPresent()){
                    College college = optionalCollege.get();
                    college.setUniversityName(requestDTO.getInstituteName());
                    college.setInstituteName(requestDTO.getInstituteName());
                    college.setLocation(requestDTO.getLocation());
                    collegeRepository.save(college);
                    education.setTypeId(college.getId());
                }
            } else if (requestDTO.getType() == EducationType.SCHOOL) {
                int schoolId = modifiedEducation.getTypeId();
                Optional<School> optionalSchool = schoolRepository.findById(schoolId);
                if(optionalSchool.isPresent()){
                    School school = optionalSchool.get();
                    school.setBoardName(requestDTO.getBoardName());
                    school.setSchoolName(requestDTO.getSchoolName());
                    school.setLocation(requestDTO.getLocation());
                    schoolRepository.save(school);
                    education.setTypeId(school.getId());
                }
            }
            return educationRepository.save(education);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EducationNotFoundException("No education found with the given id");
        }
    }

    public boolean removeEducation(Integer educationId) {
        try {
            Optional<Education> educationOptional = educationRepository.findById(educationId);
            if (educationOptional.isPresent()) {
                Education education = educationOptional.get();
                educationRepository.delete(education);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//     public Education editEducation(Education education, EducationRequest educationRequest) {
//
//     }


}