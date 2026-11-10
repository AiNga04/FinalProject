package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vti.jamie.com.testingsystem.entity.AccountAvatar;
import org.vti.jamie.com.testingsystem.util.common.EN_AvatarStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAccountAvatarRepository extends JpaRepository<AccountAvatar, Long> {
    Optional<AccountAvatar> findByAccountIdAndCurrentTrueAndStatus(Long accountId, EN_AvatarStatus status);
    List<AccountAvatar> findAllByAccountIdOrderByUploadedAtDesc(Long accountId);

    @Modifying
    @Query("update AccountAvatar a set a.current = false where a.accountId = :accountId and a.current = true")
    void clearCurrent(@Param("accountId") Long accountId);
}
