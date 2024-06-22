package com.wised.post.service;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.service.S3FileService;
import com.wised.helpandsettings.repository.DeactivateAndDeleteRepository;
import com.wised.helpandsettings.service.DeactivateAndDeleteService;
import com.wised.people.enums.ReportReason;
import com.wised.post.dtos.*;
import com.wised.post.enums.Category;
import com.wised.post.enums.LikeType;
import com.wised.post.model.*;
import com.wised.post.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final HashTagRepository hashtagRepository;
    private final MentionRepository mentionRepository;
    private final S3FileService s3FileService;

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final DeactivateAndDeleteService deactivateAndDeleteService;
    private final DeactivateAndDeleteRepository deactivateAndDeleteRepository;
    private final ReportPostRepository reportPostRepository;
    private final ShareRepository shareRepository;


    public PostResponse createpost(PostRequest postRequest, UserProfile userProfile) {
        System.out.println("entered 2");
        if (postRequest == null) {
            throw new IllegalArgumentException("Post  cannot be empty.");
        }
        List<String> awsUrls = new ArrayList<>();

        for (MultipartFile file : postRequest.getFiles()) {
            try {
                String key = generateS3Key(file.getOriginalFilename()); // Generate a unique key for the file
                s3FileService.uploadFile(key, file);
                String awsUrl = s3FileService.generatePresignedUrl(key, 3600); // Presigned URL with 1-hour expiration
                awsUrls.add(awsUrl);
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception accordingly
            }
        }
        System.out.println("entered 3");
        System.out.println(postRequest.getCategory());
        Post post = Post
                .builder()
                .views(0)
                .description(postRequest.getDescription())
                .docTitle(postRequest.getDocTitle())
                .type(postRequest.getType())
                .category(postRequest.getCategory())
                .otherCategory(postRequest.getOtherCategory())
                .awsUrl(awsUrls)
                .shareCount(0)
                .user(userProfile)
                .build();

        postRepository.save(post);

        String shareUrl = generateShareUrl(post.getId());

        post.setShareUrl(shareUrl);

        System.out.println("entered 4");
        String description = postRequest.getDescription();

        List<String> hashtags = extractHashtags(description);  //  It will work
        storeHashtags(hashtags, userProfile, post);
        List<Integer> mentionedUsers = extractMentions(postRequest.getUserMentionedIds());
        storeMentions(mentionedUsers, post);
        System.out.println("entered 5");
        PostResponseDTO postResponseDTO = PostResponseDTO.builder()
                .views(0) // Assuming new posts start with 0 views
                .description(postRequest.getDescription())
                .docTitle(postRequest.getDocTitle())
                .awsUrl(awsUrls)
                .user(userProfile.getUser().getId())
                .likes(0)
                .dislikes(0)
                .shareUrl(shareUrl)
                .userMentionedIds(mentionedUsers)
                .build();
        System.out.println("entered 6");
        List<PostResponseDTO> postsList = new ArrayList<>();
        postsList.add(postResponseDTO);

        PostResponse postResponse = PostResponse
                .builder()
                .message("post created successfully")
                .posts(postsList)
                .build();


        System.out.println("entered 7");
        return postResponse;
    }

    private String generateS3Key(String originalFilename) {
        // Implement your logic to generate a unique key (e.g., using UUID)
        return "prefix/" + originalFilename; // Example: "prefix/filename.jpg"
    }

    public List<String> extractHashtags(String description) {
        List<String> hashtags = new ArrayList<>();
        Pattern hashtagPattern = Pattern.compile("#(\\w+)");
        Matcher matcher = hashtagPattern.matcher(description);

        while (matcher.find()) {
            hashtags.add(matcher.group(1));
        }
        return hashtags;
    }

    public List<HashTag> storeHashtags(@NotNull List<String> hashtags, UserProfile userProfile, Post post) {
        List<HashTag> hashtagToMap = new ArrayList<>();
        for (String hashtag : hashtags) {
            HashTag newHashtag = HashTag.builder()
                    .hashtag(hashtag)
                    .clicksCount(0)
                    .searchCount(0)
                    .creator(userProfile)
                    .post(post)
                    .build();
            hashtagRepository.save(newHashtag);
            hashtagToMap.add(newHashtag);
        }
        return hashtagToMap;
    }

    public List<Integer> extractMentions(List<Integer> mentionedUserIds) {
        List<Integer> mentionedUsers = new ArrayList<>();

        for (Integer userId : mentionedUserIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                mentionedUsers.add(optionalUser.get().getId());
            } else {
                mentionedUsers.add(null);
            }
        }
        return mentionedUsers;
    }

    public void storeMentions(List<Integer> userIds, Post post) {
        for (Integer userId : userIds) {
            Mention newMention = Mention.builder()
                   .mentioned(userId)
                   .post(post)
                   .build();
            mentionRepository.save(newMention);
        }
    }


    public void likeDislikePost(Integer postId, UserProfile userProfile, LikeType likeType) {
        if (postId == null || userProfile == null) {
            throw new IllegalArgumentException("Post ID and User ID must not be null");
        }

        // Find the post and user
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));


        // Check if the user has already liked or disliked the post
        Like existingLike = likeRepository.findByUserAndPost(userProfile, post);

        // Handle different scenarios based on the existing like
        if (existingLike != null) {
            // If the user has already reacted to the post
            if (existingLike.getType() == likeType) {
                // If the existing reaction matches the new reaction, remove the existing reaction
                likeRepository.delete(existingLike);
            } else {
                // If the existing reaction is different from the new reaction, update the existing reaction
                existingLike.setType(likeType);
                likeRepository.save(existingLike);
            }
        } else {
            // If the user has not reacted to the post yet, create a new reaction
            Like newLike = Like.builder()
                    .user(userProfile)
                    .post(post)
                    .type(likeType)
                    .build();
            likeRepository.save(newLike);
        }
    }

    public String generateShareUrl(Integer postId){

        Post post = postRepository.findById(postId)
               .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        UserProfile userProfile = post.getUser();

        String email = userProfile.getEmail();

        String combinedString = postId.toString() + email;

        // Generate a hash using SHA-256
        String hashedString = generateHash(combinedString);

        return hashedString;

    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PostResponse getUserPosts(UserProfile viewer, UserProfile owner) {
        try {

            boolean isDeactivated = deactivateAndDeleteService.isUserProfileDeactivated(owner);
            if (isDeactivated) {
                return PostResponse.builder()
                        .error("User profile is deactivated")
                        .build();
            }

            List<Post> userPosts = postRepository.findByUser(owner);
            List<PostResponseDTO> postResponseDTOs = userPosts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return PostResponse.builder()
                    .message("User posts retrieved successfully")
                    .posts(postResponseDTOs)
                    .build();
        } catch (Exception e) {
            return PostResponse.builder()
                    .error("Failed to retrieve user posts")
                    .message(e.getMessage())
                    .build();
        }
    }
    public PostByIdResponse getPostById(int id){
        try{
            Optional<Post> optionalPost = postRepository.findById(id);
            if(optionalPost.isPresent()){
                Post post = optionalPost.get();
                return PostByIdResponse.builder()
                        .message("Post Retrieve successfully")
                        .post(convertToDTO(post))
                        .build();
            }
            else{
                return PostByIdResponse.builder()
                        .error("Post not found with the given id")
                        .build();
            }
        }catch(Exception e){
            e.printStackTrace();
            return PostByIdResponse.builder()
                    .error("Failed to retrieve post")
                    .build();
        }
    }

    public PostResponse getAllPosts() {
        try {
            List<UserProfile> activeUserProfiles = deactivateAndDeleteRepository.findByIsDeactivated(false);

            // Retrieve all posts excluding those from deactivated users
            List<Post> allPosts = postRepository.findByUserIn(activeUserProfiles);
            List<PostResponseDTO> postResponseDTOs = allPosts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return PostResponse.builder()
                    .message("All posts retrieved successfully")
                    .posts(postResponseDTOs)
                    .build();
        } catch (Exception e) {
            return PostResponse.builder()
                    .error("Failed to retrieve all posts")
                    .message(e.getMessage())
                    .build();
        }
    }

    public PostResponse reportPosts(UserProfile reportingUser, ReportPostRequest reportPostRequest){
        try {
            Optional<Post> optionalReportedPost = postRepository.findById(reportPostRequest.getReportedPostId());
            if (optionalReportedPost.isPresent()) {
                Post reportedPost = optionalReportedPost.get();
                ReportPost reportPost = ReportPost.builder()
                        .reporter(reportingUser)
                        .post(reportedPost)
                        .build();

                if (reportPostRequest.getReportReason().equals(ReportReason.OTHER)) {
                    reportPost.setReportReason(reportPostRequest.getReportReason());
                    reportPost.setOtherReason(reportPostRequest.getReportDescription());
                } else {
                    reportPost.setReportReason(reportPostRequest.getReportReason());
                }
                reportPostRepository.save(reportPost);

                PostResponse postResponse = PostResponse
                        .builder()
                        .message("Post Successfully Reported")
                        .build();
                return postResponse;

            } else {
                throw new UserNotFoundException("Post not found");
            }
        } catch (Exception e) {
            PostResponse postResponse = PostResponse
                    .builder()
                    .error("Failed to Report the Post")
                    .build();
            return postResponse;
        }
    }

    private PostResponseDTO convertToDTO(Post post) {
        int likes = likeRepository.countByPostAndType(post, LikeType.LIKE);
        int dislikes = likeRepository.countByPostAndType(post, LikeType.DISLIKE);
        List<Integer> userMentionedIds = mentionRepository.findByPost(post).stream()
                .map(Mention::getMentioned)
                .collect(Collectors.toList());


        return PostResponseDTO.builder()
                .views(post.getViews())
                .description(post.getDescription())
                .docTitle(post.getDocTitle())
                .awsUrl(post.getAwsUrl())
                .user(post.getUser().getUser().getId()) // Assuming UserProfile has a reference to User
                .likes(likes)
                .dislikes(dislikes)
                .shareUrl(post.getShareUrl())
                .postType(post.getType())
                .userMentionedIds(userMentionedIds)
                .build();
        }

    public void SharePost(Integer postId, UserProfile userProfile){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        Share share = Share
                .builder()
                .post(post)
                .user(userProfile)
                .build();

        shareRepository.save(share);

    }

}

