package com.wised.auth.dtos;
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
public class ProfessionalDetailResponse {
    private boolean success;
    private  String message;
    private String error;
    private List<ProfessionalDetailsDTO> data;
}

