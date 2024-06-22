package com.wised.auth.dtos;

import com.wised.auth.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentUploadRequest {

    private  byte[] fileBytes;
    private String originalFilename;
    private ContentType type;
    private String description;
    private String title;
    private String category;
    private String skillSet;
    private  String fileFormate;


}
