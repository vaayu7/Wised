package com.wised.auth.dtos;

import com.wised.auth.enums.PollType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PollRequest {

    private PollType type;
    private boolean isActive;
    private String question;
    private List<String> options;
    private List<String> answer;
    private boolean anonymous;

}