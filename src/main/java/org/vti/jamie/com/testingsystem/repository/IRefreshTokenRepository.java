package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vti.jamie.com.testingsystem.entity.RefreshToken;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
