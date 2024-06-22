package com.wised.auth.dtos;

import com.wised.auth.enums.EducationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationRequest {
    private Integer id;
    private String universityName;
    private String instituteName;
    private String location;
    private String boardName;
    private String schoolName;
    private String boardLocation;
    private EducationType type;
    private String specializationStream;
    private Date fromDate;
    private Date toDate;
    private boolean currentlyStudying;
    private Integer currentYearClass;
    private Integer currentSemester;
}
