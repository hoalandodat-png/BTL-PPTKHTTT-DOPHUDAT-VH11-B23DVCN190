package com.ptit.training.auth.repository;

import com.ptit.training.auth.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("""
            SELECT rt FROM RefreshToken rt
            JOIN FETCH rt.user u
            WHERE rt.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashWithUser(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revokedAt = :now, rt.revokedReason = :reason
            WHERE rt.user.id = :userId AND rt.revokedAt IS NULL
            """)
    int revokeAllActiveByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now,
            @Param("reason") String reason);
}
