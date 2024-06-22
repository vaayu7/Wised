package com.wised.people.dtos;

import com.wised.auth.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolloweeAndFollowingResponse {
    private boolean success;
    private  String message;
    private String error;
    private List<UserProfile> data;
}
