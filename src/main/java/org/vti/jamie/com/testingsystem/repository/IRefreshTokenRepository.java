package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.vti.jamie.com.testingsystem.entity.RefreshToken;

import java.util.Optional;
import java.time.Instant;
import java.util.Collection;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token join fetch token.account "
            + "where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = :now "
            + "where token.account.id in :ids and token.revokedAt is null")
    int revokeByAccountIds(@Param("ids") Collection<Long> ids, @Param("now") Instant now);
}
