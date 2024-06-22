package com.wised.auth.mappers;

import com.wised.auth.dtos.PollDto;
import com.wised.auth.model.Poll;
import org.springframework.stereotype.Component;

@Component
public class PollMapper {

    public PollDto toDto(Poll poll) {
        PollDto dto = new PollDto();
        dto.setId(poll.getId());
        dto.setUserEmail(poll.getUser().getUser().getEmail()); // Map the user's email
        dto.setType(poll.getType());
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setActive(poll.isActive());
        dto.setQuestion(poll.getQuestion());
        dto.setOptions(poll.getOptions());
        dto.setAnswer(poll.getAnswer());
        dto.setPollResponse(poll.getPollResponse());
        dto.setAnonymous(poll.isAnonymous());
        return dto;
    }
}
