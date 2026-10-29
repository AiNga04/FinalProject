package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vti.jamie.com.testingsystem.entity.RevokedSession;

public interface IRevokedSessionRepository extends JpaRepository<RevokedSession, String> {
}
