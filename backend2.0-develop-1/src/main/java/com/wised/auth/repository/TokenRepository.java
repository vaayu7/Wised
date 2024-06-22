package com.wised.auth.repository;

import com.wised.auth.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * The `TokenRepository` interface provides methods to interact with the database for token-related operations.
 * It extends the JpaRepository for basic CRUD functionality and includes custom queries.
 */
public interface TokenRepository extends JpaRepository<Token, Integer> {

    /**
     * Retrieves a list of all valid tokens associated with a specific user.
     *
     * @param userId The ID of the user for whom valid tokens are retrieved.
     * @return A list of valid tokens associated with the specified user.
     */
    @Query("""
            SELECT t FROM Token t INNER JOIN User u ON t.user.id = u.id
            WHERE u.id = :userId AND (t.expired = false OR t.revoked = false)
            """)
    List<Token> findAllValidTokenByUser(Integer userId);

    /**
     * Retrieves a token by its token value.
     *
     * @param token The token value to search for.
     * @return An optional containing the token if found, or an empty optional if not found.
     */
    Optional<Token> findByToken(String token);
}