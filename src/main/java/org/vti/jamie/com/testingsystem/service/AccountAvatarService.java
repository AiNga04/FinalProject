package org.vti.jamie.com.testingsystem.service;

import org.springframework.web.multipart.MultipartFile;
import org.vti.jamie.com.testingsystem.dto.account.AvatarResponse;

import java.util.List;
import java.nio.file.Path;

public interface AccountAvatarService {
    AvatarResponse upload(Long accountId, MultipartFile file, Long uploadedBy);
    AvatarResponse getCurrent(Long accountId);
    List<AvatarResponse> getHistory(Long accountId);
    void deleteCurrent(Long accountId, Long actorId);
    void delete(Long avatarId, Long actorId);
    AvatarResponse restore(Long avatarId, Long actorId);
    Path getFilePath(Long avatarId);
    Path getCurrentFilePath(Long accountId);
}
