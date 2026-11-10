package org.vti.jamie.com.testingsystem.mapper;

import org.springframework.stereotype.Component;
import org.vti.jamie.com.testingsystem.dto.account.AvatarResponse;
import org.vti.jamie.com.testingsystem.entity.AccountAvatar;

@Component
public class AccountAvatarMapper {
    public AvatarResponse toResponse(AccountAvatar avatar) {
        AvatarResponse response = new AvatarResponse();
        response.setId(avatar.getId());
        response.setAccountId(avatar.getAccountId());
        response.setOriginalFileName(avatar.getOriginalFileName());
        response.setContentType(avatar.getContentType());
        response.setFileSize(avatar.getFileSize());
        response.setCurrent(avatar.isCurrent());
        response.setStatus(avatar.getStatus());
        response.setUploadedBy(avatar.getUploadedBy());
        response.setUploadedAt(avatar.getUploadedAt());
        response.setDeletedAt(avatar.getDeletedAt());
        return response;
    }
}
