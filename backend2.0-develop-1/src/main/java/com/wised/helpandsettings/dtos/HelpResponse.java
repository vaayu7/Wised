package com.wised.helpandsettings.dtos;


import com.wised.auth.dtos.EducationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HelpResponse {
    private  boolean success;
    private  String message;
    private String error;
    private List<HelpResponseDTO> data;
}
