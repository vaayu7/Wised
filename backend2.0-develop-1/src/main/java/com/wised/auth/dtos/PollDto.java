package com.wised.auth.dtos;

import com.wised.auth.enums.PollType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PollDto {
    private Integer id;
    private String userEmail; // Include the user's email
    private PollType type;
    private Date createdAt;
    private boolean isActive;
    private String question;
    private List<String> options;
    private List<String> answer;
    private List<String> pollResponse;
    private boolean anonymous;
}

