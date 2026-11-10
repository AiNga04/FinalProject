package org.vti.jamie.com.testingsystem.dto.account;

import lombok.Getter;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AvatarStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class AvatarResponse {
    private Long id;
    private Long accountId;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private boolean current;
    private EN_AvatarStatus status;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
    private LocalDateTime deletedAt;
}
