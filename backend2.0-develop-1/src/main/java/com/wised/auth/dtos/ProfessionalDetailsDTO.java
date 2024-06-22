package com.wised.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfessionalDetailsDTO {
    private Integer id;
    private String companyName;
    private String industry;
    private boolean isCurrentlyEmployed;
    private String designation;
    private Date fromDate;
    private Date toDate;
    private String location;
    private Integer userId;
}
