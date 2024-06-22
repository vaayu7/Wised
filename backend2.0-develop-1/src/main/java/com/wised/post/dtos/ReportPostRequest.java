package com.wised.post.dtos;

import com.wised.people.enums.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportPostRequest {
    private Integer reportedPostId;
    private ReportReason reportReason;
    private String reportDescription;
}
