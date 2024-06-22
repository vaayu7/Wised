package com.wised.people.service;

import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.people.exception.UserBlockedException;
import com.wised.people.exception.UserNotFollowedException;
import com.wised.people.model.Block;
import com.wised.people.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserProfileRepository userProfileRepository;

    private final PeopleService peopleService;

    public boolean isUserBlocked(UserProfile blockerUserProfile, UserProfile blockedUserProfile) {
        return blockRepository.existsByBlockerAndBlocked(blockerUserProfile, blockedUserProfile);
    }
    public void blockUser(String username) throws UserBlockedException {
        try {
            System.out.println("started 3");
            UserProfile blocker = getCurrentUser().getUserProfile();

            Optional<UserProfile> optionalBlocked = userProfileRepository.findByUserName(username);
            if (optionalBlocked.isPresent()) {
                UserProfile blocked = optionalBlocked.get();
                System.out.println("started 4");
                if (!isUserBlocked(blocker, blocked)) {
                    System.out.println("started 5");
                    Block block = new Block().builder()
                            .blocker(blocker)
                            .blocked(blocked)
                            .build();
                    blockRepository.save(block);
                    peopleService.unfollowUser(blocked.getUserName());

                } else {
                    System.out.println("started 6.1");
                    // Block not found, throw a BlockNotFoundException
                    throw new UserBlockedException("Block not found");
                }
            } else {
                System.out.println("started 6.2");
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            System.out.println("started 7");
            System.out.println("Error: " + e.getMessage());
            String message = "User not found email :" + e.getMessage();
            throw new UserNotFoundException(message);
        } catch (UserBlockedException e) {
            System.out.println("started 8");
            System.out.println("Error: " + e.getMessage());

            String message = "User is  already blocked email: " + e.getMessage();
            throw  new UserBlockedException(message);
        } catch (UserNotFollowedException e) {
            e.printStackTrace();
        }
        catch (Exception e) {

            e.printStackTrace();
            throw new InternalError(e.getMessage());

        }
    }


    public void unblockUser(String username) throws UserBlockedException {
        try {

            UserProfile blocker = getCurrentUser().getUserProfile();
            Optional<UserProfile> optionalBlocked = userProfileRepository.findByUserName(username);
            if (optionalBlocked.isPresent()) {
                UserProfile blocked = optionalBlocked.get();

                Block block = blockRepository.findByBlockerAndBlocked(blocker, blocked);

                if (block != null) {
                    blockRepository.delete(block);
                } else {
                    // Block not found, throw a BlockNotFoundException
                    throw new UserBlockedException("Block not found");
                }
            } else {
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            String message = "User not found email :" + e.getMessage();
            throw new UserNotFoundException(message);

        } catch (UserBlockedException e) {

            String message = "not found for the given blocker and blocked user email: " + e.getMessage();
            throw  new UserBlockedException(message);

        } catch (Exception e) {

            e.printStackTrace();
            throw new InternalError(e.getMessage());

        }
    }
    private User getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser;
    }

}
