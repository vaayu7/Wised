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
public class UserProfileRequest {

     public Date dob;
     public String userName;
     public Gender gender;
     public String city;
     public  String country;
     public List<String> preferredLanguage;
     public List<String> genre;
     public List<String> socialMediaLinks;
     public String profileImageUrl;
     public String coverImageUrl;
     public boolean isActive;
     public String bio;
}
