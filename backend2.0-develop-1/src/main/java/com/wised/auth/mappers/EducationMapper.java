package com.wised.auth.mappers;

import com.wised.auth.dtos.EducationDTO;
import com.wised.auth.enums.EducationType;
import com.wised.auth.model.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EducationMapper {


        public static EducationDTO mapEducationToEducationDTO(Education education, College college, School school) {
            if (education.getType() == EducationType.COLLEGE) {
                return mapCollegeEducationToDTO(education, college);
            } else if (education.getType() == EducationType.SCHOOL) {
                return mapSchoolEducationToDTO(education, school);
            }
            return null;
        }

        private static EducationDTO mapCollegeEducationToDTO(Education education, College college) {
            return EducationDTO.builder()
                    .id(education.getId())
                    .type(education.getType())
                    .universityName(college.getUniversityName())
                    .instituteName(college.getInstituteName())
                    .location(college.getLocation())
                    .type(EducationType.COLLEGE)
                    .specializationStream(education.getSpecializationStream())
                    .fromDate(education.getFromDate())
                    .toDate(education.getToDate())
                    .currentlyStudying(education.isCurrentlyStudying())
                    .currentYearClass(education.getCurrentYearClass())
                    .currentSemester(education.getCurrentSemester())
                    .build();
        }

        private static EducationDTO mapSchoolEducationToDTO(Education education, School school) {
            return EducationDTO.builder()
                    .id(education.getId())
                    .type(education.getType())
                    .boardName(school.getBoardName())
                    .schoolName(school.getSchoolName())
                    .location(school.getLocation())
                    .type(EducationType.SCHOOL)
                    .specializationStream(education.getSpecializationStream())
                    .fromDate(education.getFromDate())
                    .toDate(education.getToDate())
                    .currentlyStudying(education.isCurrentlyStudying())
                    .currentYearClass(education.getCurrentYearClass())
                    .currentSemester(education.getCurrentSemester())
                    .build();
        }



//    private List<String> getNonNullList(List<String> array) {
//        if (array != null) {
//            return array;
//        } else {
//            return Collections.emptyList(); // Return an empty list if the array is null
//        }
//    }
}
