package com.wised.auth.service;

import com.wised.auth.model.Poll;
import com.wised.auth.model.User;
import com.wised.auth.model.UsertoPollMapper;
import com.wised.auth.repository.UsertoPollMapperRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class UsertoPollMapperService {

    private final UsertoPollMapperRepository mapperRepository;


    public void submitOrUpdateResponse(User user, Poll poll, List<String> userResponse) {

        Optional<UsertoPollMapper> existingMapping = mapperRepository.findByUserAndPoll(user, poll);



        if (existingMapping.isPresent()) {

            // Mapping exists, update the response

            UsertoPollMapper mapper = existingMapping.get();

            mapper.setUserResponse(userResponse);

            mapperRepository.save(mapper);

        } else {

            // Mapping doesn't exist, create a new one

            UsertoPollMapper newMapping = new UsertoPollMapper();

            newMapping.setUser(user);

            newMapping.setPoll(poll);

            newMapping.setUserResponse(userResponse);

            mapperRepository.save(newMapping);

        }

    }



    // You can add other methods as needed

}
