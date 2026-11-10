package org.vti.jamie.com.testingsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AvatarStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_avatar", indexes = {
        @Index(name = "ix_avatar_account", columnList = "account_id"),
        @Index(name = "ix_avatar_current", columnList = "account_id,is_current")
})
public class AccountAvatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAvatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EN_AvatarStatus status;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;
}
