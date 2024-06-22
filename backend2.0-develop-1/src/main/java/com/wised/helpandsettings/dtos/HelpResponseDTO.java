package com.wised.helpandsettings.dtos;


import com.wised.helpandsettings.enums.HelpStatus;
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
public class HelpResponseDTO {
    private Integer id;
    private String name;
    private String email;
    private String subject;
    private String[] issue;
    private String description;
    private List<String> awsUrl;
    private HelpStatus helpStatus;
}
