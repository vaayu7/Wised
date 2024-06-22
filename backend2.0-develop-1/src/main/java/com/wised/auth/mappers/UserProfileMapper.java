package com.wised.auth.mappers;

import com.wised.auth.model.UserProfile;
import com.wised.auth.model.UserProfileData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * A component responsible for mapping UserProfile entities to UserProfileData DTOs.
 */
@Component
public class UserProfileMapper {

    /**
     * Maps a UserProfile entity to a UserProfileData DTO.
     *
     * @param userProfile The UserProfile entity to be mapped.
     * @return A UserProfileData DTO containing the mapped data.
     */
    public UserProfileData mapUserProfileToUserProfileData(UserProfile userProfile) {
        UserProfileData userProfileData = UserProfileData.builder()
                .email(userProfile.getEmail())
                .userName(userProfile.getUserName())
                .fullName(userProfile.getFullName())
                .contact_number(userProfile.getContactNumber())
                .bio(userProfile.getBio())
                .dob(userProfile.getDob())
                .socialMediaLinks(getNonNullList(userProfile.getSocialMediaLinks()))
                .preferredLanguage(getNonNullList(userProfile.getPreferredLanguage()))
                .genre(getNonNullList(userProfile.getGenre()))
                .city(userProfile.getCity())
                .country(userProfile.getCountry())
                .coverImageUrl(userProfile.getCoverImageUrl())
                .profileImageUrl(userProfile.getProfileImageUrl())
                // Add other fields as needed
                .build();

        return userProfileData;
    }
    public UserProfileData mapUserProfileToUserProfileData(UserProfile userProfile, int followersCount, int followingCount, boolean isBlocked) {
        UserProfileData userProfileData = UserProfileData.builder()
                .email(userProfile.getEmail())
                .userName(userProfile.getUserName())
                .fullName(userProfile.getFullName())
                .bio(userProfile.getBio())
                .coverImageUrl(userProfile.getCoverImageUrl())
                .profileImageUrl(userProfile.getProfileImageUrl())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isBlocked(isBlocked)
                .build();

        return userProfileData;
    }

    /**
     * Returns a non-null list or an empty list if the input array is null.
     *
     * @param array The input array.
     * @return A List containing the input elements or an empty list if the input is null.
     */
    private List<String> getNonNullList(List<String> array) {
        if (array != null) {
            return array;
        } else {
            return Collections.emptyList(); // Return an empty list if the array is null
        }
    }
}

