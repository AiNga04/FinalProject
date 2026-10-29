package org.vti.jamie.com.testingsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "revoked_session")
public class RevokedSession {
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;
}
