package com.wised.auth.mappers;


import com.wised.auth.dtos.ContentResponse;
import com.wised.auth.model.Content;
import org.springframework.stereotype.Component;

@Component
public class ContentMapper {



    public ContentResponse mapContentToContentDto(Content content) {
        ContentResponse contentDto = new ContentResponse();
        contentDto.setId(content.getId());
        contentDto.setAwsUrl(content.getAwsUrl());
        contentDto.setKey(content.getKey());
        contentDto.setType(content.getType());
        contentDto.setDescription(content.getDescription());
        contentDto.setTitle(content.getTitle());
        contentDto.setLikes(content.getLikes());
        contentDto.setDislikes(content.getDislikes());
        contentDto.setShareUrl(content.getShareUrl());
        contentDto.setCategory(content.getCategory());
        contentDto.setRatings(content.getRatings());
        contentDto.setSkillSet(content.getSkillSet());
        contentDto.setCertificateTemplate(content.getCertificateTemplate());
        contentDto.setThumbnail(content.getThumbnail());
        contentDto.setFeedback(content.getFeedback());
        contentDto.setViews(content.getViews());
        contentDto.setShareCount(content.getShareCount());
        contentDto.setUserEmail(content.getUser().getEmail()); // Map user's email

        return contentDto;
    }

}
