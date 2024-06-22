package com.wised.auth.service;

import com.wised.auth.dtos.UserProfileRequest;
import com.wised.auth.dtos.UserProfileResponse;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.mappers.UserProfileMapper;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.model.UserProfileData;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.people.repository.BlockRepository;
import com.wised.people.repository.FollowRepository;
import com.wised.people.service.BlockService;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service class for managing user profiles.
 */
@Service
@AllArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    private final LikeRepository likeRepository;

    private final PostRepository postRepository;

    private final FollowRepository followRepository;

    private final BlockRepository blockRepository;

    private final BlockService blockService;
    private static final int NUM_SUGGESTIONS = 2;
    private static final String[] EDUCATION_TERMS = {"learn", "study", "speaks", "knowledge", "edu", "school", "class", "student", "teacher"};


    /**
     * Create a user profile for the given user.
     *
     * @param user The user for whom the profile is created.
     * @return The created user profile.
     */
    public UserProfile createProfileForUser(User user) {
//        String userName = createUserNameForUser(user);
        UserProfile userProfile = UserProfile.builder()
                .fullName(user.getFullName())
//                .userName(user.getUserame())
                .build();
        userProfile.setUser(user);
        // Set other properties for the user profile as needed
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        return savedUserProfile;
    }

//    private String createUserNameForUser(User user) {
//        String fullName = user.getFullName();
//        String email = user.getEmail();
//
//        // Concatenate fullName and email
//        String combinedData = fullName + email;
//
//        // Generate SHA-256 hash of the combined data
//        String generatedHash = generateSHA256Hash(combinedData);
//
//        // Use a longer portion of the hash as the username (e.g., first 16 characters)
//        String username = generatedHash.substring(0, 8); // Use first 16 characters of the hash
//
//        return  fullName + username;
//    }
//
//    private String generateSHA256Hash(String data) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] encodedHash = digest.digest(data.getBytes());
//
//            // Convert byte array to hexadecimal string
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : encodedHash) {
//                String hex = String.format("%02x", b);
//                hexString.append(hex);
//            }
//
//            return hexString.toString(); // Return the full hexadecimal hash
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * Update the fields of the user's profile based on the provided UserProfileRequest.
     *
     * @param userProfile    The user's profile to update.
     * @param profileRequest The UserProfileRequest containing updated profile data.
     * @return A UserProfileResponse indicating the result of the update.
     */
    public UserProfileResponse updateProfileFields(UserProfile userProfile, UserProfileRequest profileRequest) {
        try {
            if (profileRequest.getUserName() != null && !profileRequest.getUserName().equals(userProfile.getUserName())) {
                // Check if the new username is available
                boolean isUserNameAvailable = !userProfileRepository.existsByUserName(profileRequest.getUserName());
                if (!isUserNameAvailable) {
                    return UserProfileResponse.builder()
                            .success(false)
                            .message("Username is already taken")
                            .build();
                }
                // Update the username if it's available
                userProfile.setUserName(profileRequest.getUserName());
            }
            Field[] requestFields = UserProfileRequest.class.getDeclaredFields();
            for (Field requestField : requestFields) {
                // Ensure that the field is not static and is not transient
                if (!Modifier.isStatic(requestField.getModifiers()) && !Modifier.isTransient(requestField.getModifiers())) {
                    Field userProfileField;
                    try {
                        // Try to get the corresponding field in UserProfile
                        userProfileField = UserProfile.class.getDeclaredField(requestField.getName());

                        // Make the UserProfile field accessible (in case it's private)
                        userProfileField.setAccessible(true);

                        // Get the value from UserProfileRequest
                        Object value = requestField.get(profileRequest);

                        // Update the UserProfile field if the value is present
                        if (value != null) {
                            userProfileField.set(userProfile, value);
                        }
                    } catch (NoSuchFieldException e) {
                        // Handle the case where there is no corresponding field in UserProfile
                        System.out.println(e);
                        continue;
                    }
                }
            }

            UserProfile savedUserProfileData = userProfileRepository.save(userProfile); // Save the updated UserProfile object

            savedUserProfileData.setEmail(userProfile.getEmail());
            // Convert UserProfile to UserProfileData using the mapper
            UserProfileData userProfileData = userProfileMapper.mapUserProfileToUserProfileData(savedUserProfileData);

            return UserProfileResponse.builder()
                    .success(true)
                    .message("Successfully updated user profile")
                    .data(userProfileData)
                    .build();

        } catch (Exception e) {
            return UserProfileResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("Some error occurred while updating user profile")
                    .build();
        }
    }

    public boolean isUserNameAvailable(String username) {
        return !userProfileRepository.existsByUserName(username);
    }

    public List<String> suggestUniqueUsernames(String username) {
        List<String> suggestions = new ArrayList<>();

        while (suggestions.size() < NUM_SUGGESTIONS) {
            String suggestedUsername = generateRandomUsername(username);
            if (!userProfileRepository.existsByUserName(suggestedUsername) && !suggestions.contains(suggestedUsername)) {
                suggestions.add(suggestedUsername);
            }
        }

        return suggestions;
    }

    private String generateRandomUsername(String baseUsername) {
        Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(baseUsername);

        sb.append(EDUCATION_TERMS[random.nextInt(EDUCATION_TERMS.length)]);

        while (sb.length() < baseUsername.length() + 4) {
            char randomChar = characters.charAt(random.nextInt(characters.length()));
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public UserProfileData getProfile(UserProfile userProfile) {

        int following = followRepository.countByFollower(userProfile);
        int followers = followRepository.countByFollowee(userProfile);

        return userProfileMapper.mapUserProfileToUserProfileData(userProfile, following, followers, false);
    }

    public UserProfileData getProfile(UserProfile userProfile, String username) {
        Optional<UserProfile> otherUserProfileOptional = userProfileRepository.findByUserName(username);
        if (!otherUserProfileOptional.isPresent()) {
            throw new UserNotFoundException("User profile not found for username: " + username);
        }
        UserProfile blockedUserprofile = otherUserProfileOptional.get();
        int following = followRepository.countByFollower(blockedUserprofile);
        int followers = followRepository.countByFollowee(blockedUserprofile);
        if (blockService.isUserBlocked(userProfile, blockedUserprofile)) {
            return userProfileMapper.mapUserProfileToUserProfileData(blockedUserprofile, following, followers, true);
        } else {
            return userProfileMapper.mapUserProfileToUserProfileData(blockedUserprofile, following, followers, false);
        }
    }
}

