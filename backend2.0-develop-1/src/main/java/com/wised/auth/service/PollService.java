package com.wised.auth.service;


import com.wised.auth.dtos.PollRequest;
import com.wised.auth.enums.PollType;
import com.wised.auth.exception.PollNotFoundException;
import com.wised.auth.model.Poll;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.PollRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class PollService {



    private final PollRepository pollRepository;

//    @PersistenceContext
//    private EntityManager entityManager;


    public Poll createPoll(UserProfile userProfile, PollRequest pollRequest) {

        Poll poll = Poll
                .builder()
                .question(pollRequest.getQuestion())
                .options(pollRequest.getOptions())
                .answer(pollRequest.getAnswer())
                .type(pollRequest.getType())
                .pollResponse(new ArrayList<String>())
                .isActive(true)
                .user(userProfile)
                .build();

        Poll savedPoll = pollRepository.save(poll);

        return savedPoll;
    }


    public List<Poll> findAllActivePolls() {
        return pollRepository.findByIsActiveTrue();
    }

    public List<Poll> findAllPollsByUser(Integer userId) {
        return pollRepository.findByUser_Id(userId);
    }


    public boolean updateStatus(Integer pollId){

        Optional<Poll> optionalPoll =  pollRepository.findById(pollId);

        if(optionalPoll.isPresent()){

            Poll poll = optionalPoll.get();

            poll.setActive(false);

            pollRepository.save(poll);

            return true;

        }
        else{
            return false;
        }

    }

    public UserProfile getOwnerByPollId(Integer pollId){

        Optional<Poll> optionalPoll =  pollRepository.findById(pollId);

        if(optionalPoll.isPresent()){

            Poll poll = optionalPoll.get();

            return poll.getUser();

        }

        else{

            return new UserProfile();

        }

    }





    public Optional<Poll> findPollById(Integer pollId) {
        return pollRepository.findById(pollId);
    }

    public List<Poll> findPollsByQuestion(String partialQuestion) {
        return pollRepository.findByQuestionContaining(partialQuestion);
    }

    public List<Poll> findPollsByType(PollType pollType) {
        return pollRepository.findByType(pollType);
    }

    public boolean evaluateResponse(Integer pollId, List<String> response){

        Optional<Poll> optionalPoll = findPollById(pollId);

        if(optionalPoll.isPresent()){

            Poll poll =  optionalPoll.get();

            List<String> correctAnswers = poll.getAnswer();

            if(response == null){
                return false;
            }

            return response.equals(correctAnswers);

        }
        else{
            throw new PollNotFoundException(pollId);
        }

    }

}