package com.wised.auth.model;

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
public class UserProfileData {

    private String email;
    private String userName;
    private  String fullName;
    private String contact_number;

    private String bio;
    private Date dob;
    private List<String>socialMediaLinks;
    private List<String> preferredLanguage;
    private List<String> genre;
    private String city;
    private  String country;
    private String coverImageUrl;
    private String profileImageUrl;
    private int followersCount;
    private int followingCount;
    private boolean isBlocked;


}
