package com.wised.people.service;

import com.wised.auth.enums.EducationType;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.*;
import com.wised.auth.repository.*;
import com.wised.helpandsettings.model.DeactivateAndDelete;
import com.wised.helpandsettings.repository.DeactivateAndDeleteRepository;
import com.wised.notification.service.NotificationProducer;
import com.wised.people.dtos.FolloweeAndFollowingResponse;
import com.wised.people.dtos.PeopleResponse;
import com.wised.people.dtos.ReportRequest;
import com.wised.people.enums.ReportReason;
import com.wised.people.exception.UserAlreadyFollowedException;
import com.wised.people.exception.UserNotFollowedException;
import com.wised.people.model.Follow;
import com.wised.people.model.ReportUser;
import com.wised.people.repository.FollowRepository;
import com.wised.people.repository.ReportUserRepository;
import com.wised.post.enums.LikeType;
import com.wised.post.model.Post;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.aspectj.bridge.IMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.sql.Date;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PeopleService {


    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final ProfessionalDetailsRepository professionalDetailsRepository;
    private final CollegeRepository collegeRepository;
    private final SchoolRepository schoolRepository;
    private final EducationRepository educationRepository;
    private final DeactivateAndDeleteRepository deactivateAndDeleteRepository;
    private final ReportUserRepository reportUserRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final NotificationProducer notificationProducer;

    //
//    /getprofile
//    fetch the user profile from userprofilerepository
//    get the followers count and following count from follow repository
//    get all the posts of that user from postrepository
//
//    fetch the user profile from userprofilerepository
//            1)get the required data from user profile model
//            2)get the required data from education model , where we need to get the data from school and college
//            3)get the required data from professional details model
//            4)get the data from user interest model
    public void followUser(String username) throws UserAlreadyFollowedException {
        try {

            UserProfile follower = getCurrentUser().getUserProfile();
            Optional<UserProfile> optionalFollowee = userProfileRepository.findByUserName(username);
            if (optionalFollowee.isPresent()) {
                UserProfile followee = optionalFollowee.get();

                // Check if the user is already followed
                if (!isUserAlreadyFollowed(follower, followee)) {
                    // Create a new Follow entity and save it
                    Follow follow = new Follow();
                    follow.setFollower(follower);
                    follow.setFollowee(followee);
                    followRepository.save(follow);
                    String notificationMessage = follower.getUserName() + " liked your post";
                    notificationProducer.sendNotification("FOLLOW", notificationMessage);
                } else {
                    // User is already followed, handle this case if needed email
                    throw new UserAlreadyFollowedException("" + username + " is being already followed");
                }
            } else {
                throw new UserNotFoundException("" + username + " does not exist");
            }
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
            String message = "Followee not found email :" + e.getMessage();
            throw new UserNotFoundException(message);

            // Handle the UserNotFoundException as needed (log, return an error response, etc.)

        } catch (UserAlreadyFollowedException e) {
            String message = "User is already followed email :" + e.getMessage();
            throw new UserAlreadyFollowedException(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
            // Handle other exceptions (log, return an error response, etc.)
        }
    }

    public boolean isUserAlreadyFollowed(UserProfile follower, UserProfile followee) {
        // Check if there is an existing follow relationship
        return followRepository.existsByFollowerAndFollowee(follower, followee);
    }

    private User getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        return currentUser;
    }

    // 2. unfollow
    //      - check if the user is follow or not
    //      - Remove the entry from the follow table

    @Transactional
    public void unfollowUser(String username) throws UserNotFollowedException {
        try {
            UserProfile follower = getCurrentUser().getUserProfile();

            Optional<UserProfile> optionalFollowee = userProfileRepository.findByUserName(username);
            if (optionalFollowee.isPresent()) {
                UserProfile followee = optionalFollowee.get();

                // Check if the user is already followed
                if (isUserAlreadyFollowed(follower, followee)) {
                    // Unfollow the user
                    followRepository.deleteByFollowerAndFollowee(follower, followee);
                }
                if (isUserAlreadyFollowed(followee, follower)) {
                    followRepository.deleteByFollowerAndFollowee(followee, follower);
                } else {
                    // User is not followed, handle this case if needed
                    throw new UserNotFollowedException("" + username + " is not followed");
                }
            } else {
                // Followee not found, throw an exception
                throw new UserNotFoundException("" + username + " does not exist");
            }
        } catch (UserNotFollowedException e) {
            // Handle the UserNotFollowedException as needed
            System.out.println("Error: " + e.getMessage());
            String message = "No follower with email :" + e.getMessage();
            throw new UserNotFollowedException(message);

            // You might want to log the exception, return an error response, etc.
        } catch (UserNotFoundException e) {
            // Handle the UserNotFoundException as needed
            System.out.println("Error: " + e.getMessage());

            System.out.println("Error: " + e.getMessage());
            String message = "Followee not found email :" + e.getMessage();
            throw new UserNotFoundException(message);
            // You might want to log the exception, return an error response, etc.
        } catch (Exception e) {
            // Handle other exceptions as needed
            e.printStackTrace();
            throw new InternalError(e.getMessage());

            // You might want to log the exception, return an error response, etc.
        }
    }

    public PeopleResponse findUserProfilesByLocation() {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserProfile currentUserProfile = getCurrentUserProfile(currentUser.getUsername());
            String location = getLocationFromProfessionalDetails(currentUserProfile);

            if (location == null) {
                location = getLocationFromEducationDetails(currentUserProfile);
            }

            if (location == null) {
                return PeopleResponse.builder()
                        .error("No Location found")
                        .build();
            }

            List<UserProfile> userProfiles = findUserProfilesByLocation(location);
            userProfiles.removeIf(profile -> profile.equals(currentUserProfile));

            Map<UserProfile, Boolean> profileToFollowMap = new HashMap<>();

            for(UserProfile profile : userProfiles){
                if(isUserAlreadyFollowed(currentUserProfile, profile)) profileToFollowMap.put(profile, true);
                else profileToFollowMap.put(profile, false);
            }

            return PeopleResponse.builder()
                    .message("Successfully Retrieve UserProfiles List")
                    .profileToFollowMappedData(profileToFollowMap)
                    .build();
        } catch (Exception e) {
            // Log the exception and handle it gracefully
            e.printStackTrace();
            return PeopleResponse.builder()
                    .error("Failed to retrieve userprofiles")
                    .build();
        }
    }

    private UserProfile getCurrentUserProfile(String userEmail) {
        return userProfileRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    private String getLocationFromProfessionalDetails(UserProfile userProfile) {
        List<ProfessionalDetails> professionalDetails = professionalDetailsRepository.findByUserProfile(userProfile)
                .orElse(Collections.emptyList());

        if (professionalDetails.isEmpty()) {
            return null;
        }

        professionalDetails.sort(Comparator.comparing(ProfessionalDetails::getToDate).reversed());
        return professionalDetails.get(0).getLocation();
    }

    private String getLocationFromEducationDetails(UserProfile userProfile) {
        Optional<List<Education>> educationDetails = educationRepository.findByUser(userProfile);
        if (educationDetails.isEmpty()) {
            return null;
        }

        List<Education> latestEducationList = educationDetails.get();
        latestEducationList.sort(Comparator.comparing(Education::getToDate).reversed());

        Education recentEducation = latestEducationList.get(0);
        if (recentEducation.getType() == EducationType.COLLEGE) {
            return getLocationFromCollege(recentEducation.getTypeId());
        } else {
            return getLocationFromSchool(recentEducation.getTypeId());
        }
    }

    private String getLocationFromCollege(int collegeId) {
        Optional<College> optionalCollege = collegeRepository.findById(collegeId);
        if (optionalCollege.isPresent()) {
            College college = optionalCollege.get();
            return college.getLocation();
        }
        return null;
    }

    private String getLocationFromSchool(int schoolId) {
        Optional<School> optionalSchool = schoolRepository.findById(schoolId);
        if (optionalSchool.isPresent()) {
            School school = optionalSchool.get();
            return school.getLocation();
        }
        return null;
    }

    private List<UserProfile> findUserProfilesByLocation(String location) {
        List<UserProfile> userProfiles = new ArrayList<>();

        List<ProfessionalDetails> professionalDetails = professionalDetailsRepository.findByLocation(location);
        Set<UserProfile> uniqueUserProfiles = professionalDetails.stream()
                .map(ProfessionalDetails::getUserProfile)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        userProfiles.addAll(uniqueUserProfiles);
        userProfiles.addAll(findUserProfilesByEducationLocation(location, EducationType.COLLEGE));
        userProfiles.addAll(findUserProfilesByEducationLocation(location, EducationType.SCHOOL));

        return userProfiles.stream().distinct().collect(Collectors.toList());
    }

    private List<UserProfile> findUserProfilesByEducationLocation(String location, EducationType educationType) {
        List<UserProfile> userProfiles = new ArrayList<>();

        if (educationType == EducationType.COLLEGE) {
            List<College> colleges = collegeRepository.findByLocation(location);
            for (College college : colleges) {
                Education education = college.getEducation();
                userProfiles.add(education.getUser());
            }
        } else if (educationType == EducationType.SCHOOL) {
            List<School> schools = schoolRepository.findByLocation(location);
            for (School school : schools) {
                Education education = school.getEducation();
                userProfiles.add(education.getUser());
            }
        }

        return userProfiles;
    }

    public PeopleResponse findUserProfilesByUniversityOrSchool() {
        try{
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserProfile currentUserProfile = getCurrentUserProfile(currentUser.getUsername());
            String userEmail = currentUser.getUsername();
            Optional<UserProfile> user = userProfileRepository.findByUserEmail(userEmail);
            if (user.isEmpty()) {
                throw new UserNotFoundException("User Not Found");
            }

            UserProfile userProfile = user.get();
            Optional<List<Education>> educationDetails = educationRepository.findByUser(userProfile);
            if (educationDetails.isEmpty()) {
                return PeopleResponse.builder()
                        .error("No Education added")
                        .build(); // No education details found for the current user
            }
            List<Education> latestEducationList = educationDetails.get();

            latestEducationList.sort(Comparator.comparing(Education::getToDate).reversed());
            //Education latestEducation = educationDetails.get(0);

            Education recentEducation = latestEducationList.get(0);

            // Retrieve all user profiles that have the same latest institution
            //String latestInstitute = null;
            Set<UserProfile> uniqueUserProfiles = new HashSet<>();
            if (recentEducation.getType() == EducationType.COLLEGE) {
                int id = recentEducation.getTypeId();
                Optional<College> optionalCollege = collegeRepository.findById(id);
                if (optionalCollege.isPresent()) {
                    College college = optionalCollege.get();
                    String institute = college.getInstituteName();
                    List<College> allCollege = collegeRepository.findByInstituteName(institute);
                    for (College clg : allCollege) {
                        Education edu = clg.getEducation();
                        uniqueUserProfiles.add(edu.getUser());
                    }
                }
            } else {
                int id = recentEducation.getTypeId();
                Optional<School> optionalSchool = schoolRepository.findById(id);
                if (optionalSchool.isPresent()) {
                    School school = optionalSchool.get();
                    String schoolname = school.getSchoolName();
                    List<School> allSchool = schoolRepository.findBySchoolName(schoolname);
                    for (School sch : allSchool) {
                        Education edu = sch.getEducation();
                        uniqueUserProfiles.add(edu.getUser());
                    }
                }
            }
//        String latestUniversity = educationDetails.get(0).getCollege().getUniversityName();
            List<UserProfile> userProfiles = new ArrayList<>(uniqueUserProfiles);

            //Optionally, filter out the current user's profile from the results
            userProfiles.removeIf(profile -> profile.equals(userProfile));
            Map<UserProfile, Boolean> profileToFollowMap = new HashMap<>();

            for(UserProfile profile : userProfiles){
                if(isUserAlreadyFollowed(currentUserProfile, profile)) profileToFollowMap.put(profile, true);
                else profileToFollowMap.put(profile, false);
            }

            return PeopleResponse.builder()
                    .message("Successfully Retrieve UserProfiles List")
                    .profileToFollowMappedData(profileToFollowMap)
                    .build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return PeopleResponse.builder()
                    .error("Failed to retrieve userprofiles")
                    .build();
        }

    }

    public FolloweeAndFollowingResponse getFollowersList() throws UserNotFoundException {
        try {
            UserProfile followee = getCurrentUser().getUserProfile();
            Optional<List<Follow>> followers = followRepository.findByFollowee(followee);

            if (followers.isPresent() && !followers.get().isEmpty()) {
                List<Follow> followersList = followers.get();
                return FolloweeAndFollowingResponse.builder()
                        .success(true)
                        .message("Followers List Retrieve Successfully")
                        .data(followersList.stream()
                                .map(Follow::getFollower)
                                .filter(userProfile -> !isUserProfileDeactivated(userProfile))
                                .collect(Collectors.toList()))
                        .build();
            } else {
                return FolloweeAndFollowingResponse.builder()
                        .success(false)
                        .error("No Followers")
                        .build();

            }
        } catch (Exception e) {
            e.printStackTrace();
            return FolloweeAndFollowingResponse.builder()
                    .success(false)
                    .error("Failed to retrieve Followers")
                    .build();
        }
    }

    public FolloweeAndFollowingResponse getFollowingList() throws UserNotFoundException {
        try {
            UserProfile follower = getCurrentUser().getUserProfile();
            Optional<List<Follow>> followee = followRepository.findByFollower(follower);

            if (followee.isPresent() && !followee.get().isEmpty()) {
                List<Follow> followeeList = followee.get();
                return FolloweeAndFollowingResponse.builder()
                        .success(true)
                        .message("Followee List Retrieve Successfully")
                        .data(followeeList.stream()
                                .map(Follow::getFollowee)
                                .filter(userProfile -> !isUserProfileDeactivated(userProfile))
                                .collect(Collectors.toList()))
                        .build();
            } else {
                return FolloweeAndFollowingResponse.builder()
                        .success(false)
                        .error("No Following")
                        .build();

            }
        } catch (Exception e) {
            e.printStackTrace();
            return FolloweeAndFollowingResponse.builder()
                    .success(false)
                    .error("Failed to retrieve Following")
                    .build();
        }

    }

    public boolean isUserProfileDeactivated(UserProfile userProfile) {
        Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);
        return deactivateAndDeleteOptional.map(DeactivateAndDelete::isDeactivated).orElse(false);
    }

    public PeopleResponse reportUser(UserProfile reportingUser, ReportRequest reportRequest) {
        try {
            System.out.println("started");
            Optional<UserProfile> optionalReportedUser = userProfileRepository.findByUserName(reportRequest.getReportedUserName());
            if (optionalReportedUser.isPresent()) {
                UserProfile reportedUser = optionalReportedUser.get();
                System.out.println("started 2");
                ReportUser reportUser = ReportUser
                        .builder()
                        .reporter(reportingUser)
                        .reported(reportedUser)
                        .build();

                if (reportRequest.getReportReason().equals(ReportReason.OTHER)) {
                    System.out.println("started 2.1");
                    reportUser.setReportReason(reportRequest.getReportReason());
                    reportUser.setOtherReason(reportRequest.getReportDescription());
                } else {
                    System.out.println("started 2.2");
                    reportUser.setReportReason(reportRequest.getReportReason());
                }
                reportUserRepository.save(reportUser);
                System.out.println("started 3");

                PeopleResponse peopleResponse = PeopleResponse
                        .builder()
                        .message("User Successfully Reported")
                        .build();
                System.out.println("started 4");
                return peopleResponse;

            } else {
                System.out.println("started 5");
                throw new UserNotFoundException("User not found");
            }
        } catch (Exception e) {
            PeopleResponse peopleResponse = PeopleResponse
                    .builder()
                    .error("Failed to Report the User")
                    .build();
            return peopleResponse;
        }
    }

    public PeopleResponse topCreators(){
        try{

            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserProfile currentUserProfile = getCurrentUserProfile(currentUser.getUsername());

            //retrieve all the user profile based on the location
            //iterate through every profile
            //find the number of posts made by that profile in last one month
            //if the number of posts is less than 5 or and number of likes is less than 50,
            // we will not consider that user as the creator
            //else map the posts count and sum of likes of all the posts of that creator in this month to user profile and
            // increment the creator count and add the sum of likes of all the creator posts in this month to the total number of likes
            //iterate over the map and calculate using the formula and decide on the top creators
//        3 users 1,2,3. totalNumberOfCreators = 3, totalofLikes = 0 + 61 + 70 + 50
//                1 -> 6, 61
//                2-> 7, 70
//                3-> 8, 50
//
//                w1 = 6/3, w2 = 61/61+70+50
//
//                        w1*weight 1 + w2*weight2
            List<UserProfile> people = findUserProfilesByLocation().getData();
            int totalNumberOfCreators = 0;
            int totalOfLikes = 0;
            LocalDate currentDate = LocalDate.now();
            LocalDate startDate = currentDate.minusMonths(1).withDayOfMonth(1);
            Date currentDateAsDate = Date.valueOf(currentDate);
            Date startDateAsDate = Date.valueOf(startDate);
            Map<UserProfile, TopCreator> creatorMap = new HashMap<>();
            for(UserProfile user : people){
                List<Post> lastMonthPosts = postRepository.findPostsByUserAndDateRange(user, startDateAsDate, currentDateAsDate);
                int numberOfPosts = lastMonthPosts.size();
                int numberOfLikes = findNumberOfLikes(user, lastMonthPosts);
                if(numberOfPosts > 5 && numberOfLikes > 50){
                    creatorMap.put(user, new TopCreator(numberOfPosts, numberOfLikes));
                    totalNumberOfCreators++;
                    totalOfLikes += numberOfLikes;
                }
            }
            Map<UserProfile, Double> creatorWeight = new HashMap<>();
            for (UserProfile profile : creatorMap.keySet()) {
                Map.Entry entry = (Map.Entry) profile;
                TopCreator topCreator = (TopCreator) entry.getValue();
                UserProfile currentCreator = (UserProfile) entry.getKey();
                int creatorPosts = topCreator.numberOfPosts;
                int creatorLikes = topCreator.numberOfLikes;
                double w1 = (double) creatorPosts / totalNumberOfCreators;
                double w2 = (double) creatorLikes / totalOfLikes;
                double weight = w1 * 0.4 + w2 * 0.6;
                creatorWeight.put(currentCreator, weight);
            }
            List<Map.Entry<UserProfile, Double>> sortedCreators = new ArrayList<>(creatorWeight.entrySet());
            sortedCreators.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            // Get the top 10 user profiles
            List<UserProfile> top10Creators = new ArrayList<>();
            for (int i = 0; i < Math.min(10, sortedCreators.size()); i++) {
                top10Creators.add(sortedCreators.get(i).getKey());
            }

            // Output the top 10 user profiles
            for (UserProfile userProfile : top10Creators) {
                System.out.println(userProfile); // Assuming UserProfile has a meaningful toString method
            }
            Map<UserProfile, Boolean> profileToFollowMap = new HashMap<>();

            for(UserProfile profile : top10Creators){
                if(isUserAlreadyFollowed(currentUserProfile, profile)) profileToFollowMap.put(profile, true);
                else profileToFollowMap.put(profile, false);
            }

            return PeopleResponse.builder()
                    .message("Successfully Retrieve UserProfiles List")
                    .profileToFollowMappedData(profileToFollowMap)
                    .build();
        }
        catch (Exception e) {
            PeopleResponse peopleResponse = PeopleResponse
                    .builder()
                    .error("Failed to retreieve Top Creators")
                    .build();
            return peopleResponse;
        }
    }

    public int findNumberOfLikes(UserProfile user, List<Post> lastMonthPosts){
        int numberOfLikes = 0;
        for(Post post : lastMonthPosts){
            int likeCount = likeRepository.countByPostAndType(post, LikeType.LIKE);
            numberOfLikes += likeCount;
        }
        return numberOfLikes;
    }
}
