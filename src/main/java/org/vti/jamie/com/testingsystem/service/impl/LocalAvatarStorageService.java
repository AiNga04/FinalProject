package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vti.jamie.com.testingsystem.service.AvatarStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalAvatarStorageService implements AvatarStorageService {
    @Value("${app.storage.avatar-dir:./storage/avatars}")
    private String avatarDirectory;

    @Override
    public String store(MultipartFile file) throws IOException {
        Path root = Paths.get(avatarDirectory).toAbsolutePath().normalize();
        Files.createDirectories(root);
        String extension = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String key = UUID.randomUUID() + extension;
        Files.copy(file.getInputStream(), root.resolve(key));
        return key;
    }

    @Override
    public Path resolve(String storageKey) {
        Path root = Paths.get(avatarDirectory).toAbsolutePath().normalize();
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) throw new IllegalArgumentException("Storage key không hợp lệ");
        return resolved;
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }
}
