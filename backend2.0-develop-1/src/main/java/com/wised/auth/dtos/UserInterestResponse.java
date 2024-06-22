package com.wised.auth.dtos;


import com.wised.auth.enums.Gender;
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
public class UserInterestResponse {
    private String message;
    private String error;
    private List<String> data;
}
