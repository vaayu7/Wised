package com.wised.search.service;

import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.CollegeRepository;
import com.wised.auth.repository.EducationRepository;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.post.model.Post;
import com.wised.post.repository.HashTagRepository;
import com.wised.post.repository.PostRepository;
import com.wised.search.dtos.SearchResponse;
import com.wised.search.model.SearchQuery;
import com.wised.search.repository.SearchQueryRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchService {

    private final UserProfileRepository userProfileRepository;

    private final EducationRepository educationRepository;
    private final CollegeRepository collegeRepository;
    private final PostRepository postRepository;
    private final HashTagRepository hashTagRepository;
    private final SearchQueryRepository searchQueryRepository;


    public void saveSearchQuery(String query) {
        SearchQuery searchQuery = SearchQuery.builder()
                .searchQuery(query)
                .build();
        searchQueryRepository.save(searchQuery);
    }


    public SearchResponse searchUsers(String query) {
        List<UserProfile> nameMatches = userProfileRepository.findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
        List<UserProfile> bioMatches = userProfileRepository.findByBioContainingIgnoreCase(query);
        List<UserProfile> eduMatches = findUsersByEducation(query);

        List<UserProfile> allMatches = new ArrayList<>();
        allMatches.addAll(nameMatches);
        allMatches.addAll(bioMatches);
        allMatches.addAll(eduMatches);

        List<UserProfile> distinctMatches = allMatches.stream()
                .distinct()
                .collect(Collectors.toList());

        // Creating and returning the response
        SearchResponse searchResponse = SearchResponse.builder()
                .users(distinctMatches)
                .build();

        return searchResponse;
    }

    private List<UserProfile> findUsersByEducation(String university) {
        //Education recentEducation = educationRepository.findByUniversity(university);

        // Retrieve all user profiles that have the same latest institution
        //String latestInstitute = null;
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();

        List<College> allCollege = collegeRepository.findByUniversityNameContainingIgnoreCase(university);
        System.out.println("Show allcollege list"+allCollege);

        for (College clg : allCollege) {
            Education edu = clg.getEducation();
            uniqueUserProfiles.add(edu.getUser());
        }
        return uniqueUserProfiles;
    }

    private List<Post> findPostByEducation(String university) {
        //Education recentEducation = educationRepository.findByUniversity(university);

        // Retrieve all user profiles that have the same latest institution
        //String latestInstitute = null;
        List<UserProfile> uniqueUserProfiles = new ArrayList<>();
        List<College> allCollege = collegeRepository.findByUniversityNameContainingIgnoreCase(university);
        System.out.println("Show allcollege list"+allCollege);
        for (College clg : allCollege) {
            Education edu = clg.getEducation();
                uniqueUserProfiles.add(edu.getUser());
        }
        List<Post> allPosts = new ArrayList<>();
        for(UserProfile user : uniqueUserProfiles){
            List<Post> userPosts= postRepository.findByUser(user);
            allPosts.addAll(userPosts);
        }
        return allPosts;
    }


    public SearchResponse searchPosts(String query) {
        // Search in different fields
        List<Post> titleMatches = postRepository.findByDocTitleContainingIgnoreCase(query);
        List<Post> descriptionMatches = postRepository.findByDescriptionContainingIgnoreCase(query);
        List<Post> categoryMatches = postRepository.findByCategoryContainingIgnoreCase(query);
        List<Post> hashtagMatches = hashTagRepository.findByHashtagContainingIgnoreCase(query);
        List<Post> otherCategoryMatches = postRepository.findByOtherCategoryContainingIgnoreCase(query);
        List<Post> educationMatches = findPostByEducation(query);

        // Merging lists into one
        List<Post> allMatches = new ArrayList<>();
        allMatches.addAll(titleMatches);
        allMatches.addAll(descriptionMatches);
        allMatches.addAll(categoryMatches);
        allMatches.addAll(hashtagMatches);
        allMatches.addAll(educationMatches);

        // Removing duplicates using streams
        List<Post> distinctMatches = allMatches.stream()
                .distinct()
                .collect(Collectors.toList());

        // Creating and returning the response
        SearchResponse searchResponse = SearchResponse.builder()
                .posts(distinctMatches)
                .build();

        return searchResponse;
    }

    public SearchResponse searchPostAndProfileByHashTag(String query) {
        List<Post> hashtagMatches = hashTagRepository.findByHashtagContainingIgnoreCase(query);
        List<UserProfile> bioMatches = userProfileRepository.findByBioContainingIgnoreCase(query);
        SearchResponse searchResponse = SearchResponse.builder()
                .posts(hashtagMatches)
                .users(bioMatches)
                .build();
        return searchResponse;
    }

    public SearchResponse search(String query) {
        SearchResponse searchResponseUsers = searchUsers(query);
        SearchResponse searchResponsePosts = searchPosts(query);

        // Combine user and post results into a single response
        List<UserProfile> allUsers = searchResponseUsers.getUsers();
        List<Post> allPosts = searchResponsePosts.getPosts();

        SearchResponse combinedResponse = SearchResponse.builder()
                .users(allUsers)
                .posts(allPosts)
                .build();

        return combinedResponse;
    }

    public SearchResponse recentSearches(String query) {
        SearchResponse searchResponse = searchPostAndProfileByHashTag(query);
        return searchResponse;
    }

    public  SearchResponse getTrendingSearches() {

        Date now = new Date();
        Date oneDayAgo = new Date(now.getTime() - (24 * 60 * 60 * 1000));

        List<SearchQuery> recentQueries = searchQueryRepository.findByCreatedAtAfter(oneDayAgo);

        Map<String, Long> queryFrequency = recentQueries.stream()
                .collect(Collectors.groupingBy(SearchQuery::getSearchQuery, Collectors.counting()));

        List<String> trendingSearches = queryFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        SearchResponse searchResponse = SearchResponse
                .builder()
                .searchedQueries(trendingSearches)
                .build();

        return searchResponse;
    }

    public SearchResponse getRecentSearches() {
        List<SearchQuery> recentQueries = searchQueryRepository.findTop10ByOrderByCreatedAtDesc();
        List<String> recents = recentQueries.stream()
                .map(SearchQuery::getSearchQuery)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .searchedQueries(recents)
                .build();
    }
}
