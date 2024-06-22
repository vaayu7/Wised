package com.wised.auth.repository;

import com.wised.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;



/**
 * The `UserRepository` interface provides methods to interact with the database for user-related operations.
 * It extends the JpaRepository for basic CRUD functionality and includes custom queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to search for.
     * @return An optional containing the user if found, or an empty optional if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email address exists.
     *
     * @param email The email address to check for existence.
     * @return `true` if a user with the email address exists, or `false` otherwise.
     */
    Boolean existsByEmail(String email);



    /**
     * Custom Spring Data JPA repository method to find locked user accounts with expired lock times.
     *
     * @param currentTime The current time used to compare against user lock times.
     * @return A list of User entities representing locked user accounts with expired lock times.
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.lockTime < :currentTime")
    List<User> findLockedUsersWithExpiredLockTime(@Param("currentTime") Date currentTime);



    @Query(value = "SELECT * FROM _user ORDER BY RAND() LIMIT ?1", nativeQuery = true)
    List<User> findRandomUsers(int limit);


}
