package com.wised.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PollResponse {

    private String message;
    private String error;
    private boolean success;
    private PollDto response;

}