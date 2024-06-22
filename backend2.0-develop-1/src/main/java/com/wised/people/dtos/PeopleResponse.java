package com.wised.people.dtos;

import com.wised.auth.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PeopleResponse {
    private String message;
    private String error;
    private List<UserProfile> data;
    private Map<UserProfile, Boolean> profileToFollowMappedData;
}
