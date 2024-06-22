package com.wised.auth.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericResponse<T> {
    private int status;
    private String message;

    private  boolean success;

    private  String error;
    private T data;

}

