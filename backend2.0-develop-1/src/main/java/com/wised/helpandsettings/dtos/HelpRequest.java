package com.wised.helpandsettings.dtos;

import com.wised.helpandsettings.enums.HelpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HelpRequest {
    private String name;
    private String email;
    private String subject;
    private String[] issue;
    private String description;
    @NotNull
    private MultipartFile[] files;
}