package com.wised.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomLogoutResponse {
    private boolean success;
    private String message;
    private  String email;
    private  String error;
}
