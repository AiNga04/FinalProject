package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.dto.account.AvatarResponse;
import org.vti.jamie.com.testingsystem.entity.AccountAvatar;
import org.vti.jamie.com.testingsystem.exception.ResourceNotFoundException;
import org.vti.jamie.com.testingsystem.mapper.AccountAvatarMapper;
import org.vti.jamie.com.testingsystem.repository.IAccountAvatarRepository;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.service.AccountAvatarService;
import org.vti.jamie.com.testingsystem.service.AvatarStorageService;
import org.vti.jamie.com.testingsystem.util.common.EN_AvatarStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AccountAvatarServiceImpl implements AccountAvatarService {
    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final IAccountAvatarRepository avatarRepository;
    private final IAccountRepository accountRepository;
    private final AccountAvatarMapper avatarMapper;
    private final AvatarStorageService storageService;

    @Override
    @Transactional
    public AvatarResponse upload(Long accountId, MultipartFile file, Long uploadedBy) {
        validate(file);
        accountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        try {
            String key = storageService.store(file);
            avatarRepository.clearCurrent(accountId);
            AccountAvatar avatar = new AccountAvatar();
            avatar.setAccountId(accountId);
            avatar.setStorageKey(key);
            avatar.setOriginalFileName(file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename());
            avatar.setContentType(file.getContentType());
            avatar.setFileSize(file.getSize());
            avatar.setCurrent(true);
            avatar.setDefaultAvatar(false);
            avatar.setStatus(EN_AvatarStatus.ACTIVE);
            avatar.setUploadedBy(uploadedBy);
            avatar.setUploadedAt(LocalDateTime.now());
            return avatarMapper.toResponse(avatarRepository.save(avatar));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu avatar", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarResponse getCurrent(Long accountId) {
        return avatarRepository.findByAccountIdAndCurrentTrueAndStatus(accountId, EN_AvatarStatus.ACTIVE)
                .map(avatarMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarResponse> getHistory(Long accountId) {
        return avatarRepository.findAllByAccountIdOrderByUploadedAtDesc(accountId)
                .stream().map(avatarMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteCurrent(Long accountId, Long actorId) {
        AccountAvatar avatar = avatarRepository.findByAccountIdAndCurrentTrueAndStatus(accountId, EN_AvatarStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản chưa có avatar"));
        deleteAvatarFileAndRecord(avatar);
    }

    @Override
    @Transactional
    public void delete(Long avatarId, Long actorId) {
        AccountAvatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy avatar"));
        if (avatar.getStatus() == EN_AvatarStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Avatar đã được xóa trước đó");
        }
        deleteAvatarFileAndRecord(avatar);
    }

    @Override
    @Transactional
    public AvatarResponse restore(Long avatarId, Long actorId) {
        AccountAvatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy avatar"));
        if (avatar.getStatus() == EN_AvatarStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Avatar đã bị xóa vĩnh viễn và không thể khôi phục");
        }
        avatarRepository.clearCurrent(avatar.getAccountId());
        avatar.setCurrent(true);
        avatar.setStatus(EN_AvatarStatus.ACTIVE);
        avatar.setRestoredAt(LocalDateTime.now());
        avatar.setDeletedAt(null);
        return avatarMapper.toResponse(avatarRepository.save(avatar));
    }

    @Override
    @Transactional(readOnly = true)
    public Path getFilePath(Long avatarId) {
        AccountAvatar avatar = avatarRepository.findById(avatarId)
                .filter(item -> item.getStatus() == EN_AvatarStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy avatar"));
        return storageService.resolve(avatar.getStorageKey());
    }

    @Override
    @Transactional(readOnly = true)
    public Path getFilePath(Long avatarId, Long accountId) {
        AccountAvatar avatar = avatarRepository.findById(avatarId)
                .filter(item -> item.getAccountId().equals(accountId))
                .filter(item -> item.getStatus() == EN_AvatarStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy avatar"));
        return storageService.resolve(avatar.getStorageKey());
    }

    @Override
    @Transactional(readOnly = true)
    public Path getCurrentFilePath(Long accountId) {
        AccountAvatar avatar = avatarRepository
                .findByAccountIdAndCurrentTrueAndStatus(accountId, EN_AvatarStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản chưa có avatar"));
        return storageService.resolve(avatar.getStorageKey());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar không được để trống");
        if (file.getSize() > MAX_SIZE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar không được vượt quá 5MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ JPG, PNG hoặc WEBP");
    }

    private void deleteAvatarFileAndRecord(AccountAvatar avatar) {
        try {
            storageService.delete(avatar.getStorageKey());
            avatarRepository.delete(avatar);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xóa file avatar", exception);
        }
    }
}
