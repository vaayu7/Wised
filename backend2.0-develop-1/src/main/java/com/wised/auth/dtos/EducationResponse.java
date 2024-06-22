package com.wised.auth.dtos;

import com.wised.auth.enums.EducationType;
import com.wised.auth.model.UserProfileData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationResponse {

    private  boolean success;
    private  String message;
    private String error;
    private List<EducationDTO> data;

}
