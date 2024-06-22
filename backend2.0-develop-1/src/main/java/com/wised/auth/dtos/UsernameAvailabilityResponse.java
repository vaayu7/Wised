package com.wised.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsernameAvailabilityResponse {
    private boolean available;
    private List<String> suggestions;
    private String error;

}
