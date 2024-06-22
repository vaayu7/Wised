package com.wised.bystream.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamRequestDto {
   private String university;
    private String stream;
    private Integer sem;
}
