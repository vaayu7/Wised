package com.wised.bystream.service;

import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.CollegeRepository;
import com.wised.auth.repository.EducationRepository;
import com.wised.bystream.dtos.ContentDto;
import com.wised.bystream.dtos.StreamRequestDto;
import com.wised.people.service.PeopleService;
import com.wised.post.enums.LikeType;
import com.wised.post.enums.PostType;
import com.wised.post.model.Post;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import com.wised.post.repository.ShareRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ByStreamService {
    private final EducationRepository educationRepository;
    private final CollegeRepository collegeRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ShareRepository shareRepository;
    private final PeopleService peopleService;

    public ContentDto getRecommendationsForNewUser(StreamRequestDto streamRequestDto) {
        try{
            List<Post> allPosts = findPostByEducation(streamRequestDto);
            System.out.println("Show allpost list"+allPosts);
            Map<Post, Double> postWeight = calculatePostWeights(allPosts, streamRequestDto.getUniversity(), streamRequestDto.getStream());

            List<Post> top10Posts = getTopPosts(postWeight, 10);
            System.out.println("Show top list"+top10Posts);

            return ContentDto.builder()
                    .messege("Successfully retrieved top recommendation")
                    .posts(top10Posts)
                    .build();
        }catch(Exception e){
            e.printStackTrace();
            return ContentDto.builder()
                    .error("failed to retrieve top list")
                    .build();
        }

    }

    public ContentDto getRecommendationsForExistingUser(StreamRequestDto streamRequestDto) {
        List<UserProfile> following = peopleService.getFollowingList().getData();
        List<Post> allPosts = findPostByEducation(streamRequestDto);

        List<Post> filteredPosts = filterPostsByFollowing(allPosts, following);
        List<Post> topPosts;

        if (filteredPosts.size() < 10) {
            // Supplement with posts from the general list if fewer than 10 posts are found
            List<Post> remainingPosts = new ArrayList<>(allPosts);
            remainingPosts.removeAll(filteredPosts);

            int needed = 10 - filteredPosts.size();
            List<Post> additionalPosts = remainingPosts.stream()
                    .limit(needed)
                    .collect(Collectors.toList());

            filteredPosts.addAll(additionalPosts);
        }

        Map<Post, Double> postWeight = calculatePostWeights(filteredPosts, streamRequestDto.getUniversity(), streamRequestDto.getStream());
        topPosts = getTopPosts(postWeight, 10);

        return ContentDto.builder()
                .messege("Successfully retrieved top recommendation")
                .posts(topPosts)
                .build();
    }

    private List<Post> filterPostsByFollowing(List<Post> allPosts, List<UserProfile> following) {
        Set<Integer> followingIds = following.stream()
                .map(UserProfile::getId)
                .collect(Collectors.toSet());

        List<Post> filteredPosts = new ArrayList<>();
        Iterator<Post> iterator = allPosts.iterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (followingIds.contains(post.getUser().getId())) {
                filteredPosts.add(post);
                iterator.remove(); // Remove from allPosts to prevent duplication
            }
        }
        return filteredPosts;
    }

    private Map<Post, Double> calculatePostWeights(List<Post> posts, String university, String stream) {
        int totalShare = 0;
        int totalLikeCount = 0;
        LocalDateTime currentDate = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime tomorrowDateTime = currentDate.plusDays(1);
        LocalDateTime startDate = currentDate.minusDays(30);

        Date currentDateAsDate = Date.valueOf(tomorrowDateTime.toLocalDate());
        Date startDateAsDate = Date.valueOf(startDate.toLocalDate());

        Map<Post, TopShares> postMap = new HashMap<>();
        for (Post post : posts) {
            int likesCount = likeRepository.countByPostAndTypeAndDateRange(post, LikeType.LIKE, startDateAsDate, currentDateAsDate);
            int shareCount = shareRepository.countUniqueUsersByPostAndDateRange(post, startDateAsDate, currentDateAsDate);
            totalShare += shareCount;
            totalLikeCount += likesCount;
            postMap.put(post, new TopShares(shareCount, likesCount));
        }

        Map<Post, Double> postWeight = new HashMap<>();
        for (Map.Entry<Post, TopShares> entry : postMap.entrySet()) {
            Post currentPost = entry.getKey();
            TopShares topShare = entry.getValue();
            int postLikes = topShare.postLikes;
            int postShares = topShare.postShares;
            double w1 = totalShare == 0 ? 0 : (double) postShares / totalShare;
            double w2 = totalLikeCount == 0 ? 0 : (double) postLikes / totalLikeCount;
            double weight = w1 * 0.4 + w2 * 0.6;
            postWeight.put(currentPost, weight);
        }

        return postWeight;
    }

    private List<Post> getTopPosts(Map<Post, Double> postWeight, int limit) {
        return postWeight.entrySet().stream()
                .sorted(Map.Entry.<Post, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }




    private List<Post> findPostByEducation(StreamRequestDto streamRequestDto) {
        //Education recentEducation = educationRepository.findByUniversity(university);

        // Retrieve all user profiles that have the same latest institution
        //String latestInstitute = null;
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();

        List<College> allCollege = collegeRepository.findByUniversityName(streamRequestDto.getUniversity());
        System.out.println("Show allcollege list"+allCollege);

        for (College clg : allCollege) {
            Education edu = clg.getEducation();
            if(edu.getSpecializationStream().equals(streamRequestDto.getStream()) && edu.getCurrentSemester()==streamRequestDto.getSem()){
                uniqueUserProfiles.add(edu.getUser());
            }

        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUser(user);
            allPosts.addAll(userPosts);
        }

        return allPosts;

    }

    public List<Post> getNotesByUniversity(StreamRequestDto streamRequestDtoester){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<College> allCollege = collegeRepository.findByUniversityName(streamRequestDtoester.getUniversity());
        for (College clg : allCollege) {
            Education edu = clg.getEducation();
            if(edu.getSpecializationStream().equals(streamRequestDtoester.getStream()) && edu.getCurrentSemester() == streamRequestDtoester.getSem()){
                uniqueUserProfiles.add(edu.getUser());
            }
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.NOTES);
            allPosts.addAll(userPosts);
        }
        return allPosts;
    }
    public Map<String, List<Post>> getQuestionPapersByUniversity(StreamRequestDto streamRequestDtoester){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<College> allCollege = collegeRepository.findByUniversityName(streamRequestDtoester.getUniversity());
        for (College clg : allCollege) {
            Education edu = clg.getEducation();
            if(edu.getSpecializationStream().equals(streamRequestDtoester.getStream()) && edu.getCurrentSemester() == streamRequestDtoester.getSem()){
                uniqueUserProfiles.add(edu.getUser());
            }
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.QUESTION_PAPERS);
            allPosts.addAll(userPosts);
        }
        System.out.println(allPosts.size());

        return allPosts.stream().collect(Collectors.groupingBy(post -> post.getCategory().name()));
    }



    public List<Post> getWriteUpsByUniversity(StreamRequestDto streamRequestDtoester){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<College> allCollege = collegeRepository.findByUniversityName(streamRequestDtoester.getUniversity());
        for (College clg : allCollege) {
            Education edu = clg.getEducation();
            if(edu.getSpecializationStream().equals(streamRequestDtoester.getStream()) && edu.getCurrentSemester() == streamRequestDtoester.getSem()){
                uniqueUserProfiles.add(edu.getUser());
            }
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.NONE);
            allPosts.addAll(userPosts);
        }
        return allPosts;
    }

    public Map<String, List<Post>> getWriteUpsByExam(String stream){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<Education> educationList = educationRepository.findBySpecializationStream(stream);
        for (Education education : educationList) {
            uniqueUserProfiles.add(education.getUser());
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.NONE);
            allPosts.addAll(userPosts);
        }
        return allPosts.stream().collect(Collectors.groupingBy(post -> post.getCategory().name()));
    }

    public Map<String, List<Post>> getQuestionPapersByExam(String stream){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<Education> educationList = educationRepository.findBySpecializationStream(stream);
        for (Education education : educationList) {
            uniqueUserProfiles.add(education.getUser());
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.QUESTION_PAPERS);
            allPosts.addAll(userPosts);
        }
        return allPosts.stream().collect(Collectors.groupingBy(post -> post.getCategory().name()));
    }

    public List<Post> getUpToDateContentsByExam(String stream){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<Education> educationList = educationRepository.findBySpecializationStream(stream);
        for (Education education : educationList) {
            uniqueUserProfiles.add(education.getUser());
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.UP_TO_DATE_CONTENTS);
            allPosts.addAll(userPosts);
        }
        return allPosts;
    }

    public List<Post> getNotesByExam(String stream){
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<Education> educationList = educationRepository.findBySpecializationStream(stream);
        for (Education education : educationList) {
            uniqueUserProfiles.add(education.getUser());
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUserAndType(user, PostType.NOTES);
            allPosts.addAll(userPosts);
        }
        return allPosts;
    }

}
