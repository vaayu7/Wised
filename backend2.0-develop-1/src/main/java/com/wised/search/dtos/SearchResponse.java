package com.wised.search.dtos;

import com.wised.auth.model.UserProfile;
import com.wised.post.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private List<UserProfile> users;
    private List<Post> posts;
    private List<String> searchedQueries;
    private String message;
    private String error;

}
